package ro.upb.sasps.smarthome.contracts;

import java.math.BigDecimal;

/** Only public input constraints, never inventory lookup or orchestration. */
public final class ContractChecks {
  public static final double MIN_TEMPERATURE = 5.0;
  public static final double MAX_TEMPERATURE = 35.0;
  public static final int MAX_SCENE_COMMANDS = 50;

  private ContractChecks() {}

  public static void requireDeviceId(String id) {
    if (id == null || !id.matches("[a-z][a-z0-9-]{0,63}")) {
      throw new IllegalArgumentException("deviceId must match [a-z][a-z0-9-]{0,63}");
    }
  }

  public static void requireTemperature(double value) {
    if (!Double.isFinite(value)
        || value < MIN_TEMPERATURE
        || value > MAX_TEMPERATURE
        || BigDecimal.valueOf(value).stripTrailingZeros().scale() > 1) {
      throw new IllegalArgumentException("temperature must be 5–35 Celsius in steps of 0.1");
    }
  }
}
