package ro.upb.sasps.smarthome.contracts;

import java.util.List;

public record HomeSnapshot(List<DeviceSnapshot> devices) {
  public HomeSnapshot {
    devices = List.copyOf(devices);
  }
}
