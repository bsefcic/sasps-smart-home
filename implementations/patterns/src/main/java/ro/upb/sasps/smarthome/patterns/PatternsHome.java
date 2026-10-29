package ro.upb.sasps.smarthome.patterns;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.DeviceSnapshot;
import ro.upb.sasps.smarthome.contracts.ErrorCode;
import ro.upb.sasps.smarthome.contracts.HomeCommand;
import ro.upb.sasps.smarthome.contracts.HomeSnapshot;
import ro.upb.sasps.smarthome.contracts.LightSnapshot;
import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.simulators.VendorALight;

public final class PatternsHome implements SmartHome {
  private final Map<String, Light> lights = new LinkedHashMap<>();

  public PatternsHome() {
    Light light = new VendorALightAdapter(new VendorALight("living-light"));
    lights.put(light.id(), light);
  }

  @Override
  public CommandResult execute(HomeCommand command) {
    Objects.requireNonNull(command, "command");
    if (command instanceof SetLightPower request) {
      Light receiver = lights.get(request.deviceId());
      if (receiver == null) {
        return CommandResult.unknownDevice(request.deviceId());
      }
      new SetLightPowerCommand(receiver, request.power()).execute();
      return CommandResult.success();
    }
    return CommandResult.failure(
        ErrorCode.NOT_IMPLEMENTED, "Temperature control is not implemented yet.");
  }

  @Override
  public HomeSnapshot getSnapshot() {
    return new HomeSnapshot(
        lights.values().stream()
            .map(light -> (DeviceSnapshot) new LightSnapshot(light.id(), light.isOn()))
            .toList());
  }
}
