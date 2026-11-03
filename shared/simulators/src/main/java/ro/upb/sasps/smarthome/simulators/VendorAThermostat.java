package ro.upb.sasps.smarthome.simulators;

import java.util.Objects;

/** Neutral vendor API that stores target temperatures in tenths of a degree Celsius. */
public final class VendorAThermostat {
  private final String id;
  private int targetDeciCelsius = 200;

  public VendorAThermostat(String id) {
    this.id = Objects.requireNonNull(id, "id");
  }

  public String id() {
    return id;
  }

  public void setTargetDeciCelsius(int value) {
    if (value < 50 || value > 350) {
      throw new IllegalArgumentException("target temperature must be 50–350 deci-degrees Celsius");
    }
    targetDeciCelsius = value;
  }

  public int readTargetDeciCelsius() {
    return targetDeciCelsius;
  }
}
