package ro.upb.sasps.smarthome.patterns;

import java.util.Objects;
import ro.upb.sasps.smarthome.simulators.VendorALight;

public final class VendorALightAdapter implements Light {
  private final VendorALight vendor;

  public VendorALightAdapter(VendorALight vendor) {
    this.vendor = Objects.requireNonNull(vendor, "vendor");
  }

  @Override
  public String id() {
    return vendor.id();
  }

  @Override
  public void setPower(boolean power) {
    vendor.setPower(power ? VendorALight.Power.ENABLED : VendorALight.Power.DISABLED);
  }

  @Override
  public boolean isOn() {
    return vendor.readPower() == VendorALight.Power.ENABLED;
  }
}
