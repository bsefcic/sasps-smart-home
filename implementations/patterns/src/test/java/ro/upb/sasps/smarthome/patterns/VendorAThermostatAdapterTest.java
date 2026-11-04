package ro.upb.sasps.smarthome.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ro.upb.sasps.smarthome.simulators.VendorAThermostat;

class VendorAThermostatAdapterTest {
  @Test
  void exposesTheVendorIdAndInitialTemperatureInCelsius() {
    Thermostat thermostat =
        new VendorAThermostatAdapter(new VendorAThermostat("living-thermostat"));

    assertEquals("living-thermostat", thermostat.id());
    assertEquals(20.0, thermostat.targetTemperature());
  }

  @Test
  void convertsTwentyTwoPointOneCelsiusTo221DeciCelsius() {
    VendorAThermostat vendor = new VendorAThermostat("living-thermostat");
    Thermostat thermostat = new VendorAThermostatAdapter(vendor);

    thermostat.setTargetTemperature(22.1);

    assertEquals(221, vendor.readTargetDeciCelsius());
    assertEquals(22.1, thermostat.targetTemperature());
  }

  @Test
  void preservesEveryContractualTenthIncludingBothBounds() {
    VendorAThermostat vendor = new VendorAThermostat("living-thermostat");
    Thermostat thermostat = new VendorAThermostatAdapter(vendor);

    for (int deciCelsius = 50; deciCelsius <= 350; deciCelsius++) {
      double celsius = deciCelsius / 10.0;
      thermostat.setTargetTemperature(celsius);

      assertEquals(deciCelsius, vendor.readTargetDeciCelsius());
      assertEquals(celsius, thermostat.targetTemperature());
    }
  }

  @Test
  void readsCurrentVendorStateWithoutCaching() {
    VendorAThermostat vendor = new VendorAThermostat("living-thermostat");
    Thermostat thermostat = new VendorAThermostatAdapter(vendor);

    vendor.setTargetDeciCelsius(183);

    assertEquals(18.3, thermostat.targetTemperature());
  }

  @ParameterizedTest
  @ValueSource(
      doubles = {4.9, 35.1, 22.25, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void rejectsInvalidCelsiusWithoutChangingTheVendorState(double value) {
    VendorAThermostat vendor = new VendorAThermostat("living-thermostat");
    Thermostat thermostat = new VendorAThermostatAdapter(vendor);
    thermostat.setTargetTemperature(22.1);

    assertThrows(IllegalArgumentException.class, () -> thermostat.setTargetTemperature(value));

    assertEquals(221, vendor.readTargetDeciCelsius());
    assertEquals(22.1, thermostat.targetTemperature());
  }

  @Test
  void rejectsANullVendor() {
    assertThrows(NullPointerException.class, () -> new VendorAThermostatAdapter(null));
  }
}
