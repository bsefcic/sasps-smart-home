package ro.upb.sasps.smarthome.benchmarks;

import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SmartHome;

/** Alternates SetLightPower on/off for living-light, starting with on; ends with the light off. */
public final class LightToggle implements Workload {

  private final SetLightPower on = new SetLightPower(HomeChecks.LIGHT_ID, true);
  private final SetLightPower off = new SetLightPower(HomeChecks.LIGHT_ID, false);

  @Override
  public String name() {
    return "light-toggle";
  }

  @Override
  public String description() {
    return "execute(SetLightPower(living-light)) alternating on/off, starting with on;"
        + " final state: light off, thermostat unchanged";
  }

  @Override
  public String stepUnit() {
    return "command";
  }

  @Override
  public int runSteps(SmartHome home, int steps) {
    int failures = 0;
    for (int i = 0; i < steps; i++) {
      if (!home.execute((i & 1) == 0 ? on : off).ok()) {
        failures++;
      }
    }
    return failures;
  }

  @Override
  public void verifyFinal(String engine, SmartHome home) {
    HomeChecks.requireLight(engine, "final state", home, false);
    HomeChecks.requireTemperature(engine, "final state", home, HomeChecks.INITIAL_TEMPERATURE);
  }
}
