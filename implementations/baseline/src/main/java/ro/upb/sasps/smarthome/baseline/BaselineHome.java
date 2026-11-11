package ro.upb.sasps.smarthome.baseline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.DeviceSnapshot;
import ro.upb.sasps.smarthome.contracts.HomeCommand;
import ro.upb.sasps.smarthome.contracts.HomeSnapshot;
import ro.upb.sasps.smarthome.contracts.LightSnapshot;
import ro.upb.sasps.smarthome.contracts.SceneRequest;
import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SetTargetTemperature;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.contracts.ThermostatSnapshot;
import ro.upb.sasps.smarthome.simulators.VendorALight;
import ro.upb.sasps.smarthome.simulators.VendorAThermostat;

/** Direct centralized control; correctness is not intentionally weakened. */
public final class BaselineHome implements SmartHome {
  private final Map<String, VendorALight> lights = new LinkedHashMap<>();

  private final Map<String, VendorAThermostat> thermostats = new LinkedHashMap<>();

  public BaselineHome() {
    lights.put("living-light", new VendorALight("living-light"));
    thermostats.put("living-thermostat", new VendorAThermostat("living-thermostat"));
  }

  @Override
  public CommandResult execute(HomeCommand command) {
    Objects.requireNonNull(command, "command");
    CommandResult result = validate(command);
    if (result.ok()) {
      apply(command);
    }
    return result;
  }

  @Override
  public CommandResult executeScene(SceneRequest scene) {
    Objects.requireNonNull(scene, "scene");
    for (int index = 0; index < scene.commands().size(); index++) {
      CommandResult result = validate(scene.commands().get(index));
      if (!result.ok()) {
        return result.atCommand(index);
      }
    }
    for (HomeCommand command : scene.commands()) {
      apply(command);
    }
    return CommandResult.success();
  }

  private void apply(HomeCommand command) {
    if (command instanceof SetLightPower request) {
      lights
          .get(request.deviceId())
          .setPower(request.power() ? VendorALight.Power.ENABLED : VendorALight.Power.DISABLED);
    } else {
      SetTargetTemperature request = (SetTargetTemperature) command;
      thermostats
          .get(request.deviceId())
          .setTargetDeciCelsius((int) Math.round(request.temperature() * 10));
    }
  }

  private CommandResult validate(HomeCommand command) {
    if (command instanceof SetLightPower request) {
      if (lights.containsKey(request.deviceId())) {
        return CommandResult.success();
      }
      return thermostats.containsKey(request.deviceId())
          ? CommandResult.incompatibleDevice(request.deviceId(), "set-light-power")
          : CommandResult.unknownDevice(request.deviceId());
    }
    SetTargetTemperature request = (SetTargetTemperature) command;
    if (thermostats.containsKey(request.deviceId())) {
      return CommandResult.success();
    }
    return lights.containsKey(request.deviceId())
        ? CommandResult.incompatibleDevice(request.deviceId(), "set-target-temperature")
        : CommandResult.unknownDevice(request.deviceId());
  }

  @Override
  public HomeSnapshot getSnapshot() {
    ArrayList<DeviceSnapshot> devices = new ArrayList<>();
    lights
        .values()
        .forEach(
            light ->
                devices.add(
                    new LightSnapshot(
                        light.id(), light.readPower() == VendorALight.Power.ENABLED)));
    thermostats
        .values()
        .forEach(
            thermostat ->
                devices.add(
                    new ThermostatSnapshot(
                        thermostat.id(), thermostat.readTargetDeciCelsius() / 10.0)));
    devices.sort(Comparator.comparing(DeviceSnapshot::id));
    return new HomeSnapshot(devices);
  }
}
