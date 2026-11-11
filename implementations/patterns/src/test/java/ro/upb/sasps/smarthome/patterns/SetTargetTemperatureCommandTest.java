package ro.upb.sasps.smarthome.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ro.upb.sasps.smarthome.simulators.VendorAThermostat;

class SetTargetTemperatureCommandTest {
  @Test
  void defersTheEffectUntilExecution() {
    VendorAThermostat vendor = new VendorAThermostat("living-thermostat");
    Command command = new SetTargetTemperatureCommand(new VendorAThermostatAdapter(vendor), 22.1);
    assertEquals(200, vendor.readTargetDeciCelsius());
    command.execute();
    assertEquals(221, vendor.readTargetDeciCelsius());
  }

  @ParameterizedTest
  @ValueSource(
      doubles = {4.9, 35.1, 22.25, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void rejectsInvalidTemperatureBeforeExecution(double value) {
    VendorAThermostat vendor = new VendorAThermostat("living-thermostat");
    assertThrows(
        IllegalArgumentException.class,
        () -> new SetTargetTemperatureCommand(new VendorAThermostatAdapter(vendor), value));
    assertEquals(200, vendor.readTargetDeciCelsius());
  }

  @Test
  void rejectsNullReceiver() {
    assertThrows(NullPointerException.class, () -> new SetTargetTemperatureCommand(null, 22));
  }
}
