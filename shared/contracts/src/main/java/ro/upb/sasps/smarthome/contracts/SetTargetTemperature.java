package ro.upb.sasps.smarthome.contracts;

public record SetTargetTemperature(String deviceId, double temperature) implements HomeCommand {
  public SetTargetTemperature {
    ContractChecks.requireDeviceId(deviceId);
    ContractChecks.requireTemperature(temperature);
  }
}
