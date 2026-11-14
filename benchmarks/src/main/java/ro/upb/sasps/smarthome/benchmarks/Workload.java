package ro.upb.sasps.smarthome.benchmarks;

import ro.upb.sasps.smarthome.contracts.SmartHome;

/**
 * A positive benchmark scenario: every step must succeed. Each workload alternates between two
 * prebuilt steps, so an even number of steps always ends in the same, verifiable state.
 *
 * <p>The timed loop lives in each implementation on purpose: a single loop shared by all workloads
 * would make its inner call site megamorphic and add the same dispatch overhead to every step. With
 * one loop per workload, the engine call inside it only ever sees the engine types.
 */
public interface Workload {

  /** Short identifier used in the JSON and on the console, e.g. "light-toggle". */
  String name();

  /** What the workload does, recorded in the JSON. */
  String description();

  /** What one step is, e.g. "command" or "scene"; the per-step time refers to this unit. */
  String stepUnit();

  /**
   * Timed section: runs {@code steps} steps on {@code home} and returns how many results were not
   * ok. Must not print, allocate per step, or throw on a failed result.
   */
  int runSteps(SmartHome home, int steps);

  /** Called after the timer stops; throws if the final state is not the expected one. */
  void verifyFinal(String engine, SmartHome home);
}
