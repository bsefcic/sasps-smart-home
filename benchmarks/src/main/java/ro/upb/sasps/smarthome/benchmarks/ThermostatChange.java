package ro.upb.sasps.smarthome.benchmarks;

import ro.upb.sasps.smarthome.contracts.SetTargetTemperature;
import ro.upb.sasps.smarthome.contracts.SmartHome;

/**
 * Alternates SetTargetTemperature between two values that both differ from the initial 20.0 °C, so
 * every step is a real change; ends on the second value.
 */
public final class ThermostatChange implements Workload {

  static final double FIRST = 20.5;
  static final double SECOND = 21.5;

  private final SetTargetTemperature first =
      new SetTargetTemperature(HomeChecks.THERMOSTAT_ID, FIRST);
  private final SetTargetTemperature second =
      new SetTargetTemperature(HomeChecks.THERMOSTAT_ID, SECOND);

  @Override
  public String name() {
    return "thermostat";
  }

  @Override
  public String description() {
    return "execute(SetTargetTemperature(living-thermostat)) alternating "
        + FIRST
        + " / "
        + SECOND
        + " °C; final state: target "
        + SECOND
        + ", light unchanged";
  }

  @Override
  public String stepUnit() {
    return "command";
  }

  @Override
  public int runSteps(SmartHome home, int steps) {
    int failures = 0;
    for (int i = 0; i < steps; i++) {
      if (!home.execute((i & 1) == 0 ? first : second).ok()) {
        failures++;
      }
    }
    return failures;
  }

  @Override
  public void verifyFinal(String engine, SmartHome home) {
    HomeChecks.requireTemperature(engine, "final state", home, SECOND);
    HomeChecks.requireLight(engine, "final state", home, HomeChecks.INITIAL_LIGHT_POWER);
  }
}
