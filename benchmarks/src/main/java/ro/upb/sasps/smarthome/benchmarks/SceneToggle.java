package ro.upb.sasps.smarthome.benchmarks;

import java.util.List;
import ro.upb.sasps.smarthome.contracts.SceneRequest;
import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SetTargetTemperature;
import ro.upb.sasps.smarthome.contracts.SmartHome;

/**
 * Alternates two 2-command scenes, arrive-home then leave-home; ends with the light off and the
 * thermostat at the leave-home value. One step is one whole scene.
 */
public final class SceneToggle implements Workload {

  static final double ARRIVE_TEMPERATURE = 21.0;
  static final double LEAVE_TEMPERATURE = 18.0;

  private final SceneRequest arriveHome =
      new SceneRequest(
          "arrive-home",
          List.of(
              new SetLightPower(HomeChecks.LIGHT_ID, true),
              new SetTargetTemperature(HomeChecks.THERMOSTAT_ID, ARRIVE_TEMPERATURE)));

  private final SceneRequest leaveHome =
      new SceneRequest(
          "leave-home",
          List.of(
              new SetLightPower(HomeChecks.LIGHT_ID, false),
              new SetTargetTemperature(HomeChecks.THERMOSTAT_ID, LEAVE_TEMPERATURE)));

  @Override
  public String name() {
    return "scene";
  }

  @Override
  public String description() {
    return "executeScene alternating arrive-home (light on, "
        + ARRIVE_TEMPERATURE
        + " °C) / leave-home (light off, "
        + LEAVE_TEMPERATURE
        + " °C); final state: light off, target "
        + LEAVE_TEMPERATURE;
  }

  @Override
  public String stepUnit() {
    return "scene";
  }

  @Override
  public int runSteps(SmartHome home, int steps) {
    int failures = 0;
    for (int i = 0; i < steps; i++) {
      if (!home.executeScene((i & 1) == 0 ? arriveHome : leaveHome).ok()) {
        failures++;
      }
    }
    return failures;
  }

  @Override
  public void verifyFinal(String engine, SmartHome home) {
    HomeChecks.requireLight(engine, "final state", home, false);
    HomeChecks.requireTemperature(engine, "final state", home, LEAVE_TEMPERATURE);
  }
}
