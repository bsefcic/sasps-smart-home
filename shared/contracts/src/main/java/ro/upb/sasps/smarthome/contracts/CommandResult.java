package ro.upb.sasps.smarthome.contracts;

public record CommandResult(boolean ok, ErrorCode code, String message, Integer commandIndex) {
  public static CommandResult success() {
    return new CommandResult(true, null, null, null);
  }

  public static CommandResult unknownDevice(String id) {
    return failure(ErrorCode.UNKNOWN_DEVICE, "Unknown device: " + id);
  }

  public static CommandResult failure(ErrorCode code, String message) {
    return new CommandResult(false, code, message, null);
  }

  public static CommandResult incompatibleDevice(String id, String type) {
    return failure(
        ErrorCode.INCOMPATIBLE_DEVICE, "Device " + id + " does not support " + type + ".");
  }

  public CommandResult atCommand(int index) {
    if (ok || index < 0) {
      throw new IllegalArgumentException("Only an error can have a nonnegative commandIndex");
    }
    return new CommandResult(false, code, message, index);
  }
}
