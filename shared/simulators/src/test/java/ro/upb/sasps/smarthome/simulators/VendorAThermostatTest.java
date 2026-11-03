package ro.upb.sasps.smarthome.simulators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VendorAThermostatTest {
  @Test
  void preservesIdAndStartsAtTwentyDegreesCelsius() {
    VendorAThermostat thermostat = new VendorAThermostat("living-thermostat");

    assertEquals("living-thermostat", thermostat.id());
    assertEquals(200, thermostat.readTargetDeciCelsius());
  }

  @Test
  void rejectsNullIdLikeTheVendorLight() {
    assertThrows(NullPointerException.class, () -> new VendorAThermostat(null));
  }

  @ParameterizedTest
  @ValueSource(ints = {50, 221, 350})
  void acceptsBoundsAndStoresExactDeciCelsius(int value) {
    VendorAThermostat thermostat = new VendorAThermostat("living-thermostat");

    thermostat.setTargetDeciCelsius(value);

    assertEquals(value, thermostat.readTargetDeciCelsius());
  }

  @ParameterizedTest
  @ValueSource(ints = {Integer.MIN_VALUE, 49, 351, Integer.MAX_VALUE})
  void rejectsOutOfRangeValuesWithoutChangingThePreviousTarget(int value) {
    VendorAThermostat thermostat = new VendorAThermostat("living-thermostat");
    thermostat.setTargetDeciCelsius(221);

    assertThrows(IllegalArgumentException.class, () -> thermostat.setTargetDeciCelsius(value));

    assertEquals(221, thermostat.readTargetDeciCelsius());
  }

  @Test
  void instancesWithTheSameIdKeepIndependentState() {
    VendorAThermostat first = new VendorAThermostat("living-thermostat");
    VendorAThermostat second = new VendorAThermostat("living-thermostat");

    first.setTargetDeciCelsius(221);
    assertEquals(200, second.readTargetDeciCelsius());

    second.setTargetDeciCelsius(350);
    assertEquals(221, first.readTargetDeciCelsius());
    assertEquals(350, second.readTargetDeciCelsius());
  }
}
