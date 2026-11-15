package ro.upb.sasps.smarthome.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ContractV1Test {
  @ParameterizedTest
  @ValueSource(doubles = {5.0, 20.0, 22.1, 35.0})
  void acceptsExactTemperatureBoundsAndTenths(double temperature) {
    assertEquals(
        temperature, new SetTargetTemperature("living-thermostat", temperature).temperature());
  }

  @ParameterizedTest
  @ValueSource(
      doubles = {4.9, 35.1, 22.25, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void rejectsInvalidTemperature(double temperature) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SetTargetTemperature("living-thermostat", temperature));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "Living-Light", "bad id", "../light", " light", "light ", "1light"})
  void rejectsInvalidIds(String id) {
    assertThrows(IllegalArgumentException.class, () -> new SetLightPower(id, true));
    assertThrows(IllegalArgumentException.class, () -> new SetTargetTemperature(id, 20));
  }

  @Test
  void deviceIdsHaveA64CharacterLimit() {
    String longest = "a".repeat(64);
    assertEquals(longest, new SetLightPower(longest, true).deviceId());
    assertEquals(longest, new SetTargetTemperature(longest, 20).deviceId());
    assertThrows(IllegalArgumentException.class, () -> new SetLightPower(longest + "a", true));
    assertThrows(IllegalArgumentException.class, () -> new SetTargetTemperature(longest + "a", 20));
  }

  @Test
  void snapshotsDefensivelyCopyTheirDeviceList() {
    List<DeviceSnapshot> devices = new ArrayList<>();
    devices.add(new LightSnapshot("living-light", false));
    devices.add(new ThermostatSnapshot("living-thermostat", 20));
    HomeSnapshot snapshot = new HomeSnapshot(devices);
    devices.clear();
    assertEquals(
        List.of(
            new LightSnapshot("living-light", false),
            new ThermostatSnapshot("living-thermostat", 20)),
        snapshot.devices());
    assertThrows(UnsupportedOperationException.class, () -> snapshot.devices().clear());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", " ", "\t\n"})
  void rejectsMissingOrBlankSceneNames(String name) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SceneRequest(name, List.of(new SetLightPower("living-light", true))));
  }

  @Test
  void sceneNamesPreserveWhitespaceAndCountUtf16Units() {
    List<HomeCommand> commands = List.of(new SetLightPower("living-light", true));
    assertEquals(" leave-home ", new SceneRequest(" leave-home ", commands).name());
    String longest = "\uD83C\uDFE0".repeat(32);
    assertEquals(64, longest.length());
    assertEquals(longest, new SceneRequest(longest, commands).name());
    assertThrows(IllegalArgumentException.class, () -> new SceneRequest(longest + "a", commands));
  }

  @Test
  void rejectsMissingSceneCommandsAndNullElements() {
    assertThrows(IllegalArgumentException.class, () -> new SceneRequest("missing", null));
    List<HomeCommand> commands = new ArrayList<>();
    commands.add(new SetLightPower("living-light", true));
    commands.add(null);
    assertThrows(IllegalArgumentException.class, () -> new SceneRequest("null-command", commands));
  }

  @Test
  void scenesDefensivelyCopyAndHaveFixedBounds() {
    List<HomeCommand> commands = new ArrayList<>();
    commands.add(new SetLightPower("living-light", false));
    SceneRequest scene = new SceneRequest("leave-home", commands);
    commands.clear();
    assertEquals(1, scene.commands().size());
    assertThrows(UnsupportedOperationException.class, () -> scene.commands().clear());
    assertThrows(IllegalArgumentException.class, () -> new SceneRequest("empty", List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new SceneRequest("large", Collections.nCopies(51, scene.commands().get(0))));
    assertEquals(
        50,
        new SceneRequest("maximum", Collections.nCopies(50, scene.commands().get(0)))
            .commands()
            .size());
  }
}
