package ro.upb.sasps.smarthome.contracts;

public record CommandResult(boolean ok, String code, String message) {
  public static CommandResult success() {
    return new CommandResult(true, null, null);
  }

  public static CommandResult unknownDevice(String id) {
    return new CommandResult(false, "UNKNOWN_DEVICE", "Unknown device: " + id);
  }
}
