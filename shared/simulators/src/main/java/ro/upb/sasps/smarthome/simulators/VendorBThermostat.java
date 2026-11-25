package ro.upb.sasps.smarthome.simulators;

import java.util.Objects;

/** Neutral Vendor B thermostat API that stores target temperatures in degrees Fahrenheit. */
public final class VendorBThermostat {
  public static final double MIN_FAHRENHEIT = 41.0;
  public static final double MAX_FAHRENHEIT = 95.0;
  public static final double INITIAL_FAHRENHEIT = 68.0;

  private final String serialId;
  private double targetFahrenheit = INITIAL_FAHRENHEIT;

  public VendorBThermostat(String serialId) {
    this.serialId = Objects.requireNonNull(serialId, "serialId");
  }

  public String serialId() {
    return serialId;
  }

  public void applyTargetFahrenheit(double fahrenheit) {
    if (!Double.isFinite(fahrenheit)
        || fahrenheit < MIN_FAHRENHEIT
        || fahrenheit > MAX_FAHRENHEIT) {
      throw new IllegalArgumentException(
          "target temperature must be a finite value between 41.0 and 95.0 degrees Fahrenheit");
    }
    targetFahrenheit = fahrenheit;
  }

  public double getTargetFahrenheit() {
    return targetFahrenheit;
  }
}
