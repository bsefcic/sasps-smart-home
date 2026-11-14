package ro.upb.sasps.smarthome.benchmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import ro.upb.sasps.smarthome.baseline.BaselineHome;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.patterns.PatternsHome;

/**
 * Direct engine benchmark, without Spring/HTTP. For each workload, every engine gets the same
 * warm-up and the same measured rounds on fresh instances, with the engine order alternating
 * between rounds. Only the workload's step loop is timed; state checks happen outside the timer.
 */
public final class RuntimeBenchmark {

  public record Config(int warmupSteps, int rounds, int stepsPerRound) {
    public static final Config DEFAULT = new Config(200_000, 10, 1_000_000);

    public Config {
      if (warmupSteps <= 0 || rounds <= 0 || stepsPerRound <= 0) {
        throw new IllegalArgumentException("all parameters must be positive");
      }
      if (warmupSteps % 2 != 0 || stepsPerRound % 2 != 0) {
        throw new IllegalArgumentException(
            "step counts must be even so every workload ends in its known final state");
      }
    }
  }

  public record Engine(String name, Supplier<SmartHome> factory) {
    public Engine {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(factory, "factory");
    }
  }

  /**
   * One measured round; order lists engines in execution order (alternating), while ms always lists
   * them in the fixed engine order. Times are measured with System.nanoTime and converted to
   * milliseconds without rounding.
   */
  public record Round(int round, List<String> order, Map<String, Double> ms) {}

  /** All times in milliseconds, except the per-step median, which is in nanoseconds. */
  public record EngineSummary(
      List<Double> rawMs, double medianMs, double minMs, double maxMs, double medianNsPerStep) {}

  public record WorkloadResult(
      String description,
      String stepUnit,
      List<String> warmupOrder,
      List<Round> rounds,
      Map<String, EngineSummary> engines) {}

  /* workloads is keyed by workload name, in execution order. */
  public record Result(Config config, Map<String, WorkloadResult> workloads) {}

  private final Config config;
  private final List<Engine> engines;
  private final List<Workload> workloads;

  public RuntimeBenchmark(Config config, List<Engine> engines, List<Workload> workloads) {
    this.config = Objects.requireNonNull(config, "config");
    if (engines.isEmpty() || workloads.isEmpty()) {
      throw new IllegalArgumentException("at least one engine and one workload are required");
    }
    this.engines = List.copyOf(engines);
    this.workloads = List.copyOf(workloads);
  }

  public static List<Engine> defaultEngines() {
    return List.of(
        new Engine("baseline", BaselineHome::new), new Engine("patterns", PatternsHome::new));
  }

  public static List<Workload> defaultWorkloads() {
    return List.of(new LightToggle(), new ThermostatChange(), new SceneToggle());
  }

  public Result runBenchmarks() {
    Map<String, WorkloadResult> results = new LinkedHashMap<>();
    for (Workload workload : workloads) {
      results.put(workload.name(), runWorkload(workload));
    }
    return new Result(config, results);
  }

  private WorkloadResult runWorkload(Workload workload) {
    List<String> warmupOrder = new ArrayList<>();
    for (Engine engine : engines) {
      measureRound(engine, workload, config.warmupSteps());
      warmupOrder.add(engine.name());
    }

    List<Round> rounds = new ArrayList<>();
    for (int round = 0; round < config.rounds(); round++) {
      List<String> names = new ArrayList<>();
      Map<String, Double> measured = new HashMap<>();
      for (Engine engine : orderForRound(round)) {
        measured.put(engine.name(), toMs(measureRound(engine, workload, config.stepsPerRound())));
        names.add(engine.name());
      }
      // add the engines to the ms map in a consistent order, instead of alternating, as they are in measured
      Map<String, Double> ms = new LinkedHashMap<>();
      for (Engine engine : engines) {
        ms.put(engine.name(), measured.get(engine.name()));
      }
      rounds.add(new Round(round + 1, List.copyOf(names), ms));
    }

    Map<String, EngineSummary> summaries = new LinkedHashMap<>();
    for (Engine engine : engines) {
      List<Double> raw = rounds.stream().map(round -> round.ms().get(engine.name())).toList();
      Stats stats = Stats.of(raw);
      summaries.put(
          engine.name(),
          new EngineSummary(
              raw,
              stats.median(),
              stats.min(),
              stats.max(),
              stats.median() * 1e6 / config.stepsPerRound()));
    }
    return new WorkloadResult(
        workload.description(), workload.stepUnit(), List.copyOf(warmupOrder), rounds, summaries);
  }

  static double toMs(long nanos) {
    return nanos / 1e6;
  }

  /** Rotates the engine list by one position per round; with two engines this alternates. */
  List<Engine> orderForRound(int round) {
    List<Engine> order = new ArrayList<>(engines);
    Collections.rotate(order, -(round % engines.size()));
    return order;
  }

  /**
   * Runs the workload on a fresh instance and returns elapsed nanoseconds. The initial state is
   * checked before the timer starts; failed results and the final state are checked after it stops,
   * so an invalid run is never recorded.
   */
  static long measureRound(Engine engine, Workload workload, int steps) {
    SmartHome home = Objects.requireNonNull(engine.factory().get(), "engine factory result");
    HomeChecks.requireInitialState(engine.name(), home);

    long start = System.nanoTime();
    int failures = workload.runSteps(home, steps);
    long elapsed = System.nanoTime() - start;

    if (failures != 0) {
      throw new IllegalStateException(
          engine.name()
              + " "
              + workload.name()
              + ": "
              + failures
              + " of "
              + steps
              + " steps failed");
    }
    workload.verifyFinal(engine.name(), home);
    return elapsed;
  }
}
