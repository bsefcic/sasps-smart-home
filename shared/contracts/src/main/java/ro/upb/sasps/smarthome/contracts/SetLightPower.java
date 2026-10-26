package ro.upb.sasps.smarthome.contracts;

import java.util.Objects;

public record SetLightPower(String deviceId, boolean power) implements HomeCommand {
  public SetLightPower {
    Objects.requireNonNull(deviceId, "deviceId");
  }
}
