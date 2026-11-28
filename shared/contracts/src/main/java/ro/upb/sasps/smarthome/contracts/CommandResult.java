package ro.upb.sasps.smarthome.contracts;

import java.util.List;

public record CommandResult(
    boolean ok, ErrorCode code, String message, Integer commandIndex, List<Integer> commandPath) {
  public CommandResult {
    if (commandPath != null) {
      commandPath = List.copyOf(commandPath);
    }
  }

  public static CommandResult success() {
    return new CommandResult(true, null, null, null, null);
  }

  public static CommandResult unknownDevice(String id) {
    return failure(ErrorCode.UNKNOWN_DEVICE, "Unknown device: " + id);
  }

  public static CommandResult failure(ErrorCode code, String message) {
    return new CommandResult(false, code, message, null, null);
  }

  public static CommandResult incompatibleDevice(String id, String type) {
    return failure(
        ErrorCode.INCOMPATIBLE_DEVICE, "Device " + id + " does not support " + type + ".");
  }

  public static CommandResult unknownScene(String id) {
    return failure(ErrorCode.UNKNOWN_SCENE, "Unknown scene: " + id);
  }

  public static CommandResult sceneCycle(String id) {
    return failure(ErrorCode.SCENE_CYCLE, "Scene cycle: " + id);
  }

  public static CommandResult sceneDepthExceeded() {
    return failure(
        ErrorCode.SCENE_LIMIT_EXCEEDED,
        "Scenes can be nested at most " + ContractChecks.MAX_SCENE_DEPTH + " levels deep.");
  }

  public static CommandResult sceneSizeExceeded() {
    return failure(
        ErrorCode.SCENE_LIMIT_EXCEEDED,
        "A scene can expand to at most " + ContractChecks.MAX_SCENE_COMMANDS + " commands.");
  }

  public static CommandResult unreachableScene(String id) {
    return failure(ErrorCode.INVALID_SCENE, "Scene definition is not reachable: " + id);
  }

  public CommandResult atCommand(int index) {
    if (ok || index < 0) {
      throw new IllegalArgumentException("Only an error can have a nonnegative commandIndex");
    }
    return new CommandResult(false, code, message, index, null);
  }

  public CommandResult atPath(List<Integer> path) {
    if (ok
        || path == null
        || path.isEmpty()
        || path.size() > ContractChecks.MAX_SCENE_DEPTH
        || path.stream().anyMatch(index -> index == null || index < 0)) {
      throw new IllegalArgumentException("Only an error can have a commandPath of 1–5 indices");
    }
    return new CommandResult(false, code, message, null, path);
  }
}
