package ro.upb.sasps.smarthome.baseline;

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

/** Direct centralized control; correctness is not intentionally weakened. */
public final class BaselineHome implements SmartHome {
  private final Map<String, VendorALight> lights = new LinkedHashMap<>();

  public BaselineHome() {
    lights.put("living-light", new VendorALight("living-light"));
  }

  @Override
  public CommandResult execute(HomeCommand command) {
    Objects.requireNonNull(command, "command");
    if (command instanceof SetLightPower request) {
      VendorALight light = lights.get(request.deviceId());
      if (light == null) {
        return CommandResult.unknownDevice(request.deviceId());
      }
      light.setPower(request.power() ? VendorALight.Power.ENABLED : VendorALight.Power.DISABLED);
      return CommandResult.success();
    }
    return CommandResult.failure(
        ErrorCode.NOT_IMPLEMENTED, "Temperature control is not implemented yet.");
  }

  @Override
  public HomeSnapshot getSnapshot() {
    return new HomeSnapshot(
        lights.values().stream()
            .map(
                light ->
                    (DeviceSnapshot)
                        new LightSnapshot(
                            light.id(), light.readPower() == VendorALight.Power.ENABLED))
            .toList());
  }
}
