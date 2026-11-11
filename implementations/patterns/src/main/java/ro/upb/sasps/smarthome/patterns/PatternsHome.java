package ro.upb.sasps.smarthome.patterns;

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

public final class PatternsHome implements SmartHome {
  private final Map<String, Light> lights = new LinkedHashMap<>();

  private final Map<String, Thermostat> thermostats = new LinkedHashMap<>();

  public PatternsHome() {
    Light light = new VendorALightAdapter(new VendorALight("living-light"));
    lights.put(light.id(), light);
    thermostats.put(
        "living-thermostat",
        new VendorAThermostatAdapter(new VendorAThermostat("living-thermostat")));
  }

  @Override
  public CommandResult execute(HomeCommand command) {
    Objects.requireNonNull(command, "command");
    CommandResult result = validate(command);
    if (result.ok()) {
      buildCommand(command).execute();
    }
    return result;
  }

  @Override
  public CommandResult executeScene(SceneRequest scene) {
    Objects.requireNonNull(scene, "scene");
    ArrayList<Command> commands = new ArrayList<>();
    for (int index = 0; index < scene.commands().size(); index++) {
      HomeCommand request = scene.commands().get(index);
      CommandResult result = validate(request);
      if (!result.ok()) {
        return result.atCommand(index);
      }
      commands.add(buildCommand(request));
    }
    new SceneCommand(commands).execute();
    return CommandResult.success();
  }

  private Command buildCommand(HomeCommand command) {
    if (command instanceof SetLightPower request) {
      return new SetLightPowerCommand(lights.get(request.deviceId()), request.power());
    }
    SetTargetTemperature request = (SetTargetTemperature) command;
    return new SetTargetTemperatureCommand(
        thermostats.get(request.deviceId()), request.temperature());
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
    lights.values().forEach(light -> devices.add(new LightSnapshot(light.id(), light.isOn())));
    thermostats
        .values()
        .forEach(
            thermostat ->
                devices.add(
                    new ThermostatSnapshot(thermostat.id(), thermostat.targetTemperature())));
    devices.sort(Comparator.comparing(DeviceSnapshot::id));
    return new HomeSnapshot(devices);
  }
}
