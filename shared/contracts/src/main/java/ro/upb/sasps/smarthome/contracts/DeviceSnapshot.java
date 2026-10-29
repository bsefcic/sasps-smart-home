package ro.upb.sasps.smarthome.contracts;

public sealed interface DeviceSnapshot permits LightSnapshot, ThermostatSnapshot {
  String id();

  String kind();
}
