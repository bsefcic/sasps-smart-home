package ro.upb.sasps.smarthome.benchmarks;

import ro.upb.sasps.smarthome.contracts.DeviceSnapshot;
import ro.upb.sasps.smarthome.contracts.LightSnapshot;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.contracts.ThermostatSnapshot;

/** State checks done outside the timed section, through the public snapshot only. */
final class HomeChecks {

  static final String LIGHT_ID = "living-light";
  static final String THERMOSTAT_ID = "living-thermostat";

  /** Contract v1 initial state: light off, thermostat at 20.0 °C. */
  static final boolean INITIAL_LIGHT_POWER = false;

  static final double INITIAL_TEMPERATURE = 20.0;

  private static final double TOLERANCE = 1e-9;

  private HomeChecks() {}

  static void requireInitialState(String engine, SmartHome home) {
    requireLight(engine, "initial state", home, INITIAL_LIGHT_POWER);
    requireTemperature(engine, "initial state", home, INITIAL_TEMPERATURE);
  }

  static void requireLight(String engine, String when, SmartHome home, boolean expected) {
    LightSnapshot light = find(engine, when, home, LightSnapshot.class, LIGHT_ID);
    if (light.power() != expected) {
      throw new IllegalStateException(
          engine + " " + when + ": " + LIGHT_ID + " power is " + light.power());
    }
  }

  static void requireTemperature(String engine, String when, SmartHome home, double expected) {
    ThermostatSnapshot thermostat =
        find(engine, when, home, ThermostatSnapshot.class, THERMOSTAT_ID);
    if (Math.abs(thermostat.targetTemperature() - expected) > TOLERANCE) {
      throw new IllegalStateException(
          engine
              + " "
              + when
              + ": "
              + THERMOSTAT_ID
              + " target is "
              + thermostat.targetTemperature()
              + ", expected "
              + expected);
    }
  }

  private static <T extends DeviceSnapshot> T find(
      String engine, String when, SmartHome home, Class<T> type, String id) {
    for (DeviceSnapshot device : home.getSnapshot().devices()) {
      if (type.isInstance(device) && id.equals(device.id())) {
        return type.cast(device);
      }
    }
    throw new IllegalStateException(engine + " " + when + ": " + id + " not found");
  }
}
