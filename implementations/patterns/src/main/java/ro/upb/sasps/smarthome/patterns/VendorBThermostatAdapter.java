package ro.upb.sasps.smarthome.patterns;

import java.util.Objects;
import ro.upb.sasps.smarthome.contracts.ContractChecks;
import ro.upb.sasps.smarthome.simulators.VendorBThermostat;

/**
 * Adapts Vendor B's Fahrenheit thermostat API to the application's Celsius Thermostat interface.
 */
public final class VendorBThermostatAdapter implements Thermostat {
  private final VendorBThermostat vendor;

  public VendorBThermostatAdapter(VendorBThermostat vendor) {
    this.vendor = Objects.requireNonNull(vendor, "vendor");
  }

  @Override
  public String id() {
    return vendor.serialId();
  }

  @Override
  public void setTargetTemperature(double value) {
    ContractChecks.requireTemperature(value);
    vendor.applyTargetFahrenheit(value * 9.0 / 5.0 + 32.0);
  }

  @Override
  public double targetTemperature() {
    double celsius = (vendor.getTargetFahrenheit() - 32.0) * 5.0 / 9.0;
    return Math.round(celsius * 10.0) / 10.0;
  }
}
