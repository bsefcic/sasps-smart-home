package ro.upb.sasps.smarthome.benchmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import ro.upb.sasps.smarthome.baseline.BaselineHome;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.DeviceSnapshot;
import ro.upb.sasps.smarthome.contracts.LightSnapshot;
import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.patterns.PatternsHome;

/**
 * Direct engine benchmark, without Spring/HTTP. Every engine receives the same workload:
 * alternating on/off commands for living-light, starting with on. Even command counts therefore end
 * with the light off, which is verified outside the timed section.
 */
public final class RuntimeBenchmark {

  public static final String DEVICE_ID = "living-light";

  public record Config(int warmupCommands, int rounds, int commandsPerRound) {
    public static final Config DEFAULT = new Config(200_000, 10, 1_000_000);

    public Config {
      if (warmupCommands <= 0 || rounds <= 0 || commandsPerRound <= 0) {
        throw new IllegalArgumentException("all parameters must be positive");
      }
      if (warmupCommands % 2 != 0 || commandsPerRound % 2 != 0) {
        throw new IllegalArgumentException("command counts must be even so the light ends off");
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
   * One measured round; order lists engines in execution order, ms is keyed by engine. Times are
   * measured with System.nanoTime and converted to milliseconds without rounding.
   */
  public record Round(int round, List<String> order, Map<String, Double> ms) {}

  /** All times in milliseconds, except the per-command median, which is in nanoseconds. */
  public record EngineSummary(
      List<Double> rawMs, double medianMs, double minMs, double maxMs, double medianNsPerCommand) {}

  public record Result(
      Config config,
      String workload,
      List<String> warmupOrder,
      List<Round> rounds,
      Map<String, EngineSummary> engines) {}

  private final Config config;
  private final List<Engine> engines;

  public RuntimeBenchmark(Config config, List<Engine> engines) {
    this.config = Objects.requireNonNull(config, "config");
    if (engines.isEmpty()) {
      throw new IllegalArgumentException("at least one engine is required");
    }
    this.engines = List.copyOf(engines);
  }

  public static List<Engine> defaultEngines() {
    return List.of(
        new Engine("baseline", BaselineHome::new), new Engine("patterns", PatternsHome::new));
  }

  public Result run() {
    List<String> warmupOrder = new ArrayList<>();
    for (Engine engine : engines) {
      SmartHome home = freshHome(engine);
      measure(engine.name(), home, config.warmupCommands());
      warmupOrder.add(engine.name());
    }

    List<Round> rounds = new ArrayList<>();
    for (int round = 0; round < config.rounds(); round++) {
      List<Engine> order = orderForRound(round);
      List<String> names = new ArrayList<>();
      Map<String, Double> ms = new LinkedHashMap<>();
      for (Engine engine : order) {
        SmartHome home = freshHome(engine);
        ms.put(engine.name(), toMs(measure(engine.name(), home, config.commandsPerRound())));
        names.add(engine.name());
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
              stats.median() * 1e6 / config.commandsPerRound()));
    }
    return new Result(config, workloadDescription(), List.copyOf(warmupOrder), rounds, summaries);
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

  private static SmartHome freshHome(Engine engine) {
    SmartHome home = Objects.requireNonNull(engine.factory().get(), "engine factory result");
    requireLightPower(engine.name(), home, false, "initial state");
    return home;
  }

  /**
   * Runs the workload and returns elapsed nanoseconds. Only the command loop is timed; validation
   * of results and final state happens after the timer stops.
   */
  static long measure(String engineName, SmartHome home, int commands) {
    SetLightPower on = new SetLightPower(DEVICE_ID, true);
    SetLightPower off = new SetLightPower(DEVICE_ID, false);
    int failures = 0;

    long start = System.nanoTime();
    for (int i = 0; i < commands; i++) {
      CommandResult result = home.execute((i & 1) == 0 ? on : off);
      if (!result.ok()) {
        failures++;
      }
    }
    long elapsed = System.nanoTime() - start;

    if (failures != 0) {
      throw new IllegalStateException(
          engineName + ": " + failures + " of " + commands + " commands failed");
    }
    requireLightPower(engineName, home, false, "final state");
    return elapsed;
  }

  private static void requireLightPower(
      String engineName, SmartHome home, boolean expected, String when) {
    for (DeviceSnapshot device : home.getSnapshot().devices()) {
      if (device instanceof LightSnapshot light && DEVICE_ID.equals(light.id())) {
        if (light.power() != expected) {
          throw new IllegalStateException(
              engineName + " " + when + ": " + DEVICE_ID + " power is " + light.power());
        }
        return;
      }
    }
    throw new IllegalStateException(engineName + " " + when + ": " + DEVICE_ID + " not found");
  }

  private static String workloadDescription() {
    return "SetLightPower(" + DEVICE_ID + ") alternating on/off, starting with on; final state off";
  }
}
