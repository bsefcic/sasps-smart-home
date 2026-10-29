package ro.upb.sasps.smarthome.contracts;

public record ThermostatSnapshot(String id, String kind, double targetTemperature)
    implements DeviceSnapshot {
  public ThermostatSnapshot(String id, double targetTemperature) {
    this(id, "thermostat", targetTemperature);
  }

  public ThermostatSnapshot {
    ContractChecks.requireDeviceId(id);
    ContractChecks.requireTemperature(targetTemperature);
    if (!"thermostat".equals(kind)) {
      throw new IllegalArgumentException("kind must be thermostat");
    }
  }
}
