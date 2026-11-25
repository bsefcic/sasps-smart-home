package ro.upb.sasps.smarthome.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ro.upb.sasps.smarthome.simulators.VendorBThermostat;

class VendorBThermostatAdapterTest {
  @Test
  void exposesTheVendorSerialIdAndInitialTemperatureInCelsius() {
    Thermostat thermostat =
        new VendorBThermostatAdapter(new VendorBThermostat("bedroom-thermostat"));

    assertEquals("bedroom-thermostat", thermostat.id());
    assertEquals(20.0, thermostat.targetTemperature());
  }

  @Test
  void convertsTwentyTwoPointOneCelsiusToFahrenheitAndBack() {
    VendorBThermostat vendor = new VendorBThermostat("bedroom-thermostat");
    Thermostat thermostat = new VendorBThermostatAdapter(vendor);

    thermostat.setTargetTemperature(22.1);

    assertEquals(71.78, vendor.getTargetFahrenheit(), 1e-9);
    assertEquals(22.1, thermostat.targetTemperature());
  }

  @Test
  void preservesEveryContractualTenthIncludingBothBounds() {
    VendorBThermostat vendor = new VendorBThermostat("bedroom-thermostat");
    Thermostat thermostat = new VendorBThermostatAdapter(vendor);

    for (int deciCelsius = 50; deciCelsius <= 350; deciCelsius++) {
      double celsius = deciCelsius / 10.0;
      double expectedFahrenheit = celsius * 9.0 / 5.0 + 32.0;

      thermostat.setTargetTemperature(celsius);

      assertEquals(expectedFahrenheit, vendor.getTargetFahrenheit(), 1e-9);
      assertEquals(celsius, thermostat.targetTemperature());
    }
  }

  @Test
  void readsCurrentVendorStateWithoutCaching() {
    VendorBThermostat vendor = new VendorBThermostat("bedroom-thermostat");
    Thermostat thermostat = new VendorBThermostatAdapter(vendor);

    vendor.applyTargetFahrenheit(64.94);

    assertEquals(18.3, thermostat.targetTemperature());
  }

  @ParameterizedTest
  @ValueSource(
      doubles = {4.9, 35.1, 22.25, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void rejectsInvalidCelsiusWithoutChangingTheVendorState(double value) {
    VendorBThermostat vendor = new VendorBThermostat("bedroom-thermostat");
    Thermostat thermostat = new VendorBThermostatAdapter(vendor);
    thermostat.setTargetTemperature(22.1);

    assertThrows(IllegalArgumentException.class, () -> thermostat.setTargetTemperature(value));

    assertEquals(71.78, vendor.getTargetFahrenheit(), 1e-9);
    assertEquals(22.1, thermostat.targetTemperature());
  }

  @Test
  void rejectsANullVendor() {
    assertThrows(NullPointerException.class, () -> new VendorBThermostatAdapter(null));
  }
}
