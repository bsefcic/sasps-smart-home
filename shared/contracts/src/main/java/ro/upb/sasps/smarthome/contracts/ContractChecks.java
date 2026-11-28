package ro.upb.sasps.smarthome.contracts;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/** Only public input constraints, never inventory lookup or orchestration. */
public final class ContractChecks {
  public static final double MIN_TEMPERATURE = 5.0;
  public static final double MAX_TEMPERATURE = 35.0;
  public static final int MAX_SCENE_COMMANDS = 50;
  public static final int MAX_SCENE_DEFINITIONS = 50;
  public static final int MAX_SCENE_DEPTH = 5;
  public static final int MAX_SCENE_NAME_LENGTH = 64;

  private ContractChecks() {}

  public static void requireDeviceId(String id) {
    requireId(id, "deviceId");
  }

  public static void requireSceneId(String id) {
    requireId(id, "sceneId");
  }

  public static void requireSceneName(String name) {
    if (name == null || name.isBlank() || name.length() > MAX_SCENE_NAME_LENGTH) {
      throw new IllegalArgumentException("name must be nonblank and at most 64 characters");
    }
  }

  public static void requireSceneList(List<?> entries) {
    if (entries == null
        || entries.isEmpty()
        || entries.size() > MAX_SCENE_COMMANDS
        || entries.stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("commands must contain 1–50 nonnull commands");
    }
  }

  private static void requireId(String id, String field) {
    if (id == null || !id.matches("[a-z][a-z0-9-]{0,63}")) {
      throw new IllegalArgumentException(field + " must match [a-z][a-z0-9-]{0,63}");
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
