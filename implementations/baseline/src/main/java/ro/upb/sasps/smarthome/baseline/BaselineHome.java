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
import ro.upb.sasps.smarthome.simulators.VendorBThermostat;

/** Direct centralized control; correctness is not intentionally weakened. */
public final class BaselineHome implements SmartHome {
  private final Map<String, VendorALight> lights = new LinkedHashMap<>();

  private final Map<String, VendorAThermostat> vendorAThermostats = new LinkedHashMap<>();

  private final Map<String, VendorBThermostat> vendorBThermostats = new LinkedHashMap<>();

  private final Map<String, String> motionSensorLights = new LinkedHashMap<>();

  private final Map<String, Boolean> motionReadings = new LinkedHashMap<>();

  public BaselineHome() {
    lights.put("living-light", new VendorALight("living-light"));
    vendorAThermostats.put("living-thermostat", new VendorAThermostat("living-thermostat"));
    vendorBThermostats.put("bedroom-thermostat", new VendorBThermostat("bedroom-thermostat"));
    motionSensorLights.put("living-motion", "living-light");
    motionReadings.put("living-motion", false);
  }

  @Override
  public CommandResult reportMotion(String sensorId, boolean motion) {
    if (!motionSensorLights.containsKey(sensorId)) {
      return CommandResult.unknownDevice(sensorId);
    }
    boolean started = motion && !motionReadings.get(sensorId);
    motionReadings.put(sensorId, motion);
    if (started) {
      lights.get(motionSensorLights.get(sensorId)).setPower(VendorALight.Power.ENABLED);
    }
    return CommandResult.success();
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
      VendorAThermostat vendorA = vendorAThermostats.get(request.deviceId());
      if (vendorA != null) {
        vendorA.setTargetDeciCelsius((int) Math.round(request.temperature() * 10));
      } else {
        vendorBThermostats
            .get(request.deviceId())
            .applyTargetFahrenheit(request.temperature() * 9.0 / 5.0 + 32.0);
      }
    }
  }

  private CommandResult validate(HomeCommand command) {
    if (command instanceof SetLightPower request) {
      if (lights.containsKey(request.deviceId())) {
        return CommandResult.success();
      }
      return hasThermostat(request.deviceId())
          ? CommandResult.incompatibleDevice(request.deviceId(), "set-light-power")
          : CommandResult.unknownDevice(request.deviceId());
    }
    SetTargetTemperature request = (SetTargetTemperature) command;
    if (hasThermostat(request.deviceId())) {
      return CommandResult.success();
    }
    return lights.containsKey(request.deviceId())
        ? CommandResult.incompatibleDevice(request.deviceId(), "set-target-temperature")
        : CommandResult.unknownDevice(request.deviceId());
  }

  private boolean hasThermostat(String deviceId) {
    return vendorAThermostats.containsKey(deviceId) || vendorBThermostats.containsKey(deviceId);
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
    vendorAThermostats
        .values()
        .forEach(
            thermostat ->
                devices.add(
                    new ThermostatSnapshot(
                        thermostat.id(), thermostat.readTargetDeciCelsius() / 10.0)));
    vendorBThermostats
        .values()
        .forEach(
            thermostat -> {
              double celsius = (thermostat.getTargetFahrenheit() - 32.0) * 5.0 / 9.0;
              devices.add(
                  new ThermostatSnapshot(thermostat.serialId(), Math.round(celsius * 10.0) / 10.0));
            });
    devices.sort(Comparator.comparing(DeviceSnapshot::id));
    return new HomeSnapshot(devices);
  }
}
