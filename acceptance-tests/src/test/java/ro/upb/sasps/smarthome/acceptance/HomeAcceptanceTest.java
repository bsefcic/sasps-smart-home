package ro.upb.sasps.smarthome.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ro.upb.sasps.smarthome.baseline.BaselineHome;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.HomeCommand;
import ro.upb.sasps.smarthome.contracts.HomeSnapshot;
import ro.upb.sasps.smarthome.contracts.LightSnapshot;
import ro.upb.sasps.smarthome.contracts.SceneRequest;
import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SetTargetTemperature;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.contracts.ThermostatSnapshot;
import ro.upb.sasps.smarthome.patterns.PatternsHome;

class HomeAcceptanceTest {
  static Stream<Arguments> implementations() {
    return Stream.of(
        Arguments.of("baseline", (Supplier<SmartHome>) BaselineHome::new),
        Arguments.of("patterns", (Supplier<SmartHome>) PatternsHome::new));
  }

  @ParameterizedTest(name = "{0}: on/off")
  @MethodSource("implementations")
  void appliesPowerCommands(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    assertFalse(((LightSnapshot) home.getSnapshot().devices().get(0)).power());
    assertEquals(CommandResult.success(), home.execute(new SetLightPower("living-light", true)));
    assertTrue(((LightSnapshot) home.getSnapshot().devices().get(0)).power());
    home.execute(new SetLightPower("living-light", false));
    assertFalse(((LightSnapshot) home.getSnapshot().devices().get(0)).power());
  }

  @ParameterizedTest(name = "{0}: unknown device")
  @MethodSource("implementations")
  void rejectsUnknownDeviceWithoutSideEffects(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    HomeSnapshot before = home.getSnapshot();
    assertEquals(
        CommandResult.unknownDevice("missing"), home.execute(new SetLightPower("missing", true)));
    assertEquals(before, home.getSnapshot());
  }

  @ParameterizedTest(name = "{0}: isolated state")
  @MethodSource("implementations")
  void protectsSnapshotsAndInstances(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    HomeSnapshot before = home.getSnapshot();
    assertThrows(UnsupportedOperationException.class, () -> before.devices().clear());
    home.execute(new SetLightPower("living-light", true));
    assertFalse(((LightSnapshot) before.devices().get(0)).power());
    assertFalse(((LightSnapshot) factory.get().getSnapshot().devices().get(0)).power());
  }

  @ParameterizedTest(name = "{0}: initial inventory and temperature control")
  @MethodSource("implementations")
  void controlsTemperatureAndPreservesSnapshots(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    HomeSnapshot initial = home.getSnapshot();
    assertEquals(
        new HomeSnapshot(
            List.of(
                new LightSnapshot("living-light", false),
                new ThermostatSnapshot("living-thermostat", 20.0))),
        initial);
    for (double temperature : new double[] {5.0, 35.0, 22.1, 22.1}) {
      assertEquals(
          CommandResult.success(),
          home.execute(new SetTargetTemperature("living-thermostat", temperature)));
      assertEquals(
          new ThermostatSnapshot("living-thermostat", temperature),
          home.getSnapshot().devices().get(1));
      assertEquals(new LightSnapshot("living-light", false), home.getSnapshot().devices().get(0));
    }
    assertEquals(new ThermostatSnapshot("living-thermostat", 20.0), initial.devices().get(1));
    assertEquals(initial, factory.get().getSnapshot());
  }

  @ParameterizedTest(name = "{0}: inventory errors have no effects")
  @MethodSource("implementations")
  void rejectsWrongTypesAndUnknownThermostats(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    home.execute(new SetLightPower("living-light", true));
    home.execute(new SetTargetTemperature("living-thermostat", 22.1));
    HomeSnapshot before = home.getSnapshot();
    assertEquals(
        CommandResult.unknownDevice("missing"),
        home.execute(new SetTargetTemperature("missing", 22)));
    assertEquals(
        CommandResult.incompatibleDevice("living-light", "set-target-temperature"),
        home.execute(new SetTargetTemperature("living-light", 22)));
    assertEquals(
        CommandResult.incompatibleDevice("living-thermostat", "set-light-power"),
        home.execute(new SetLightPower("living-thermostat", true)));
    assertEquals(before, home.getSnapshot());
    for (double invalid :
        new double[] {
          4.9, 35.1, 22.25, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        }) {
      assertThrows(
          IllegalArgumentException.class,
          () -> home.execute(new SetTargetTemperature("living-thermostat", invalid)));
      assertEquals(before, home.getSnapshot());
    }
  }

  @ParameterizedTest(name = "{0}: mixed scenes preserve order")
  @MethodSource("implementations")
  void executesMixedScenesInOrder(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    assertEquals(
        CommandResult.success(),
        home.executeScene(
            new SceneRequest(
                "leave-home",
                List.of(
                    new SetLightPower("living-light", true),
                    new SetTargetTemperature("living-thermostat", 22.1),
                    new SetLightPower("living-light", false),
                    new SetTargetTemperature("living-thermostat", 18)))));
    assertEquals(
        new HomeSnapshot(
            List.of(
                new LightSnapshot("living-light", false),
                new ThermostatSnapshot("living-thermostat", 18))),
        home.getSnapshot());
    assertEquals(
        CommandResult.success(),
        home.executeScene(
            new SceneRequest("single", List.of(new SetLightPower("living-light", true)))));
    List<HomeCommand> commands = new ArrayList<>();
    for (int i = 0; i < 50; i++)
      commands.add(new SetTargetTemperature("living-thermostat", i % 2 == 0 ? 5 : 35));
    assertEquals(CommandResult.success(), home.executeScene(new SceneRequest("maximum", commands)));
    assertEquals(
        new ThermostatSnapshot("living-thermostat", 35), home.getSnapshot().devices().get(1));
  }

  @ParameterizedTest(name = "{0}: scene errors never partially apply")
  @MethodSource("implementations")
  void prevalidatesEverySceneCommand(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    home.execute(new SetTargetTemperature("living-thermostat", 22.1));
    HomeSnapshot before = home.getSnapshot();
    Map<HomeCommand, CommandResult> invalid =
        Map.of(
            new SetLightPower("missing", true),
            CommandResult.unknownDevice("missing"),
            new SetTargetTemperature("missing", 18),
            CommandResult.unknownDevice("missing"),
            new SetLightPower("living-thermostat", true),
            CommandResult.incompatibleDevice("living-thermostat", "set-light-power"),
            new SetTargetTemperature("living-light", 18),
            CommandResult.incompatibleDevice("living-light", "set-target-temperature"));
    for (var entry : invalid.entrySet()) {
      for (int index = 0; index < 3; index++) {
        List<HomeCommand> commands =
            new ArrayList<>(
                List.of(
                    new SetLightPower("living-light", true),
                    new SetTargetTemperature("living-thermostat", 18)));
        commands.add(index, entry.getKey());
        assertEquals(
            entry.getValue().atCommand(index),
            home.executeScene(new SceneRequest("invalid", commands)));
        assertEquals(before, home.getSnapshot());
      }
    }
    assertEquals(
        CommandResult.unknownDevice("missing").atCommand(1),
        home.executeScene(
            new SceneRequest(
                "first-error",
                List.of(
                    new SetLightPower("living-light", true),
                    new SetLightPower("missing", false),
                    new SetTargetTemperature("living-light", 18)))));
    assertEquals(before, home.getSnapshot());
  }
}
