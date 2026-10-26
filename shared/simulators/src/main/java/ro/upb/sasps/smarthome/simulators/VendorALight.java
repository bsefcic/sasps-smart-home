package ro.upb.sasps.smarthome.simulators;

import java.util.Objects;

/** Neutral vendor API, deliberately different from the public application contract. */
public final class VendorALight {
  public enum Power {
    ENABLED,
    DISABLED
  }

  private final String id;
  private Power power = Power.DISABLED;

  public VendorALight(String id) {
    this.id = Objects.requireNonNull(id, "id");
  }

  public String id() {
    return id;
  }

  public void setPower(Power value) {
    power = Objects.requireNonNull(value, "power");
  }

  public Power readPower() {
    return power;
  }
}
