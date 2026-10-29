package ro.upb.sasps.smarthome.contracts;

public record SetLightPower(String deviceId, boolean power) implements HomeCommand {
  public SetLightPower {
    ContractChecks.requireDeviceId(deviceId);
  }
}
