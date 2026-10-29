package ro.upb.sasps.smarthome.contracts;

public record LightSnapshot(String id, String kind, boolean power) implements DeviceSnapshot {
  public LightSnapshot(String id, boolean power) {
    this(id, "light", power);
  }

  public LightSnapshot {
    ContractChecks.requireDeviceId(id);
    if (!"light".equals(kind)) {
      throw new IllegalArgumentException("kind must be light");
    }
  }
}
