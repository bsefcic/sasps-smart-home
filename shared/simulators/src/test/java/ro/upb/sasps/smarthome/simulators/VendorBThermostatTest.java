package ro.upb.sasps.smarthome.simulators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VendorBThermostatTest {
  @Test
  void preservesSerialIdAndStartsAtSixtyEightFahrenheit() {
    VendorBThermostat thermostat = new VendorBThermostat("bedroom-thermostat");

    assertEquals("bedroom-thermostat", thermostat.serialId());
    assertEquals(68.0, thermostat.getTargetFahrenheit());
  }

  @Test
  void rejectsNullSerialId() {
    assertThrows(NullPointerException.class, () -> new VendorBThermostat(null));
  }

  @ParameterizedTest
  @ValueSource(doubles = {41.0, 68.0, 71.78, 95.0})
  void acceptsBoundsAndStoresExactFahrenheit(double value) {
    VendorBThermostat thermostat = new VendorBThermostat("bedroom-thermostat");

    thermostat.applyTargetFahrenheit(value);

    assertEquals(value, thermostat.getTargetFahrenheit());
  }

  @ParameterizedTest
  @ValueSource(
      doubles = {
        -100.0,
        40.99,
        95.01,
        150.0,
        Double.NaN,
        Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY
      })
  void rejectsInvalidFahrenheitWithoutChangingPreviousTarget(double value) {
    VendorBThermostat thermostat = new VendorBThermostat("bedroom-thermostat");
    thermostat.applyTargetFahrenheit(71.78);

    assertThrows(IllegalArgumentException.class, () -> thermostat.applyTargetFahrenheit(value));

    assertEquals(71.78, thermostat.getTargetFahrenheit());
  }

  @Test
  void instancesWithTheSameSerialIdKeepIndependentState() {
    VendorBThermostat first = new VendorBThermostat("bedroom-thermostat");
    VendorBThermostat second = new VendorBThermostat("bedroom-thermostat");

    first.applyTargetFahrenheit(71.78);
    assertEquals(68.0, second.getTargetFahrenheit());

    second.applyTargetFahrenheit(95.0);
    assertEquals(71.78, first.getTargetFahrenheit());
    assertEquals(95.0, second.getTargetFahrenheit());
  }
}
