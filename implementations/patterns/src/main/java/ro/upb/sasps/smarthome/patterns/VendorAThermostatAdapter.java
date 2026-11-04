package ro.upb.sasps.smarthome.patterns;

import java.util.Objects;
import ro.upb.sasps.smarthome.contracts.ContractChecks;
import ro.upb.sasps.smarthome.simulators.VendorAThermostat;

public final class VendorAThermostatAdapter implements Thermostat {
  private final VendorAThermostat vendor;

  public VendorAThermostatAdapter(VendorAThermostat vendor) {
    this.vendor = Objects.requireNonNull(vendor, "vendor");
  }

  @Override
  public String id() {
    return vendor.id();
  }

  @Override
  public void setTargetTemperature(double value) {
    ContractChecks.requireTemperature(value);
    vendor.setTargetDeciCelsius((int) Math.round(value * 10));
  }

  @Override
  public double targetTemperature() {
    return vendor.readTargetDeciCelsius() / 10.0;
  }
}
