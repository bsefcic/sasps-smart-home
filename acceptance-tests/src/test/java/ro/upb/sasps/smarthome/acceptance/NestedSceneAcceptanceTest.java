package ro.upb.sasps.smarthome.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.ErrorCode;
import ro.upb.sasps.smarthome.contracts.HomeSnapshot;
import ro.upb.sasps.smarthome.contracts.InvalidSceneEntry;
import ro.upb.sasps.smarthome.contracts.LightSnapshot;
import ro.upb.sasps.smarthome.contracts.NestedSceneRequest;
import ro.upb.sasps.smarthome.contracts.SceneEntry;
import ro.upb.sasps.smarthome.contracts.SceneReference;
import ro.upb.sasps.smarthome.contracts.SceneResult;
import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SetTargetTemperature;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.contracts.ThermostatSnapshot;

class NestedSceneAcceptanceTest {
  private static final String IMPLEMENTATIONS =
      "ro.upb.sasps.smarthome.acceptance.HomeAcceptanceTest#implementations";
  private static final SceneEntry LIGHT_ON = new SetLightPower("living-light", true);
  private static final SceneEntry LIGHT_OFF = new SetLightPower("living-light", false);

  private record Case(String name, NestedSceneRequest request, CommandResult expected) {}

  @ParameterizedTest(name = "{0}: three levels with a reused subscene")
  @MethodSource(IMPLEMENTATIONS)
  void executesTheContractExample(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();

    assertEquals(
        SceneResult.success(3),
        home.executeNestedScene(
            scene(
                List.of(ref("warm"), bedroom(18), ref("warm")),
                "warm",
                List.of(ref("bedroom")),
                "bedroom",
                List.of(bedroom(22.1)))));
    assertEquals(
        new HomeSnapshot(
            List.of(
                new ThermostatSnapshot("bedroom-thermostat", 22.1),
                new LightSnapshot("living-light", false),
                new ThermostatSnapshot("living-thermostat", 20.0))),
        home.getSnapshot());
  }

  @ParameterizedTest(name = "{0}: depth-first, left to right, every occurrence counts")
  @MethodSource(IMPLEMENTATIONS)
  void executesDepthFirstInDefinedOrder(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    // a → b: living 23, bedroom 24; bedroom 21 | living 19 | b | a → bedroom 21 last.
    assertEquals(
        SceneResult.success(9),
        home.executeNestedScene(
            scene(
                List.of(ref("a"), living(19), ref("b"), ref("a")),
                "a",
                List.of(ref("b"), bedroom(21)),
                "b",
                List.of(living(23), bedroom(24)))));
    assertEquals(23, temperature(home, "living-thermostat"));
    assertEquals(21, temperature(home, "bedroom-thermostat"));

    assertEquals(
        SceneResult.success(3),
        home.executeNestedScene(
            scene(List.of(ref("b"), living(19)), "b", List.of(living(23), bedroom(24)))));
    assertEquals(19, temperature(home, "living-thermostat"));
    assertEquals(24, temperature(home, "bedroom-thermostat"));
  }

  @ParameterizedTest(name = "{0}: depth 5 and 50 expanded commands are accepted")
  @MethodSource(IMPLEMENTATIONS)
  void acceptsTheLimits(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    assertEquals(
        SceneResult.success(1),
        home.executeNestedScene(
            scene(
                List.of(ref("a")),
                "a",
                List.of(ref("b")),
                "b",
                List.of(ref("c")),
                "c",
                List.of(ref("d")),
                "d",
                List.of(LIGHT_ON))));
    assertEquals(true, light(home));

    List<SceneEntry> ten = new ArrayList<>();
    for (int index = 0; index < 10; index++) {
      ten.add(index % 2 == 0 ? LIGHT_OFF : LIGHT_ON);
    }
    assertEquals(
        SceneResult.success(50),
        home.executeNestedScene(scene(Collections.nCopies(5, ref("ten")), "ten", ten)));
    assertEquals(true, light(home));
  }

  @ParameterizedTest(name = "{0}: rejected nested scenes have no effects")
  @MethodSource(IMPLEMENTATIONS)
  void rejectsInvalidScenesWithoutEffects(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    home.execute(new SetTargetTemperature("bedroom-thermostat", 19.5));
    HomeSnapshot before = home.getSnapshot();
    for (Case invalid : invalidScenes()) {
      assertEquals(
          SceneResult.failure(invalid.expected()),
          home.executeNestedScene(invalid.request()),
          invalid.name());
      assertEquals(before, home.getSnapshot(), invalid.name());
    }
  }

  private static List<Case> invalidScenes() {
    SceneEntry badTemperature =
        new InvalidSceneEntry(ErrorCode.INVALID_TEMPERATURE, "temperature must be 5–35");
    List<Case> cases = new ArrayList<>();
    cases.add(
        new Case(
            "missing reference",
            scene(List.of(LIGHT_ON, ref("unknown"))),
            CommandResult.unknownScene("unknown").atPath(List.of(1))));
    cases.add(
        new Case(
            "the label is not a definition",
            new NestedSceneRequest("warm", List.of(ref("warm")), Map.of()),
            CommandResult.unknownScene("warm").atPath(List.of(0))));
    cases.add(
        new Case(
            "direct cycle",
            scene(List.of(LIGHT_ON, ref("loop")), "loop", List.of(ref("loop"))),
            CommandResult.sceneCycle("loop").atPath(List.of(1, 0))));
    cases.add(
        new Case(
            "indirect cycle",
            scene(
                List.of(ref("first")),
                "first",
                List.of(ref("second")),
                "second",
                List.of(ref("first"))),
            CommandResult.sceneCycle("first").atPath(List.of(0, 0, 0))));
    cases.add(
        new Case(
            "depth six",
            scene(
                List.of(ref("a")),
                "a",
                List.of(ref("b")),
                "b",
                List.of(ref("c")),
                "c",
                List.of(ref("d")),
                "d",
                List.of(ref("e")),
                "e",
                List.of(LIGHT_ON)),
            CommandResult.sceneDepthExceeded().atPath(List.of(0, 0, 0, 0, 0))));
    cases.add(
        new Case(
            "a reused definition is checked again at a deeper position",
            scene(
                List.of(ref("a"), ref("d1")),
                "a",
                List.of(ref("b")),
                "b",
                List.of(LIGHT_ON),
                "d1",
                List.of(ref("d2")),
                "d2",
                List.of(ref("d3")),
                "d3",
                List.of(ref("a"))),
            CommandResult.sceneDepthExceeded().atPath(List.of(1, 0, 0, 0, 0))));
    cases.add(
        new Case(
            "54 expanded commands",
            scene(
                List.of(ref("a"), ref("a"), ref("a")),
                "a",
                List.of(ref("b"), ref("b"), ref("b")),
                "b",
                List.of(ref("c"), ref("c"), ref("c")),
                "c",
                List.of(LIGHT_ON, LIGHT_OFF)),
            CommandResult.sceneSizeExceeded().atPath(List.of(2, 2, 1, 0))));
    cases.add(
        new Case(
            "invalid command deep in the structure",
            scene(
                List.of(LIGHT_ON, ref("warm")),
                "warm",
                List.of(ref("bedroom")),
                "bedroom",
                List.of(badTemperature)),
            CommandResult.failure(ErrorCode.INVALID_TEMPERATURE, "temperature must be 5–35")
                .atPath(List.of(1, 0, 0))));
    cases.add(
        new Case(
            "unknown device in the first occurrence of a reused subscene",
            scene(
                List.of(ref("warm"), bedroom(18), ref("warm")),
                "warm",
                List.of(ref("bedroom")),
                "bedroom",
                List.of(new SetTargetTemperature("missing", 22.1))),
            CommandResult.unknownDevice("missing").atPath(List.of(0, 0, 0))));
    cases.add(
        new Case(
            "incompatible device in a subscene",
            scene(
                List.of(ref("warm")),
                "warm",
                List.of(LIGHT_ON, new SetLightPower("bedroom-thermostat", true))),
            CommandResult.incompatibleDevice("bedroom-thermostat", "set-light-power")
                .atPath(List.of(0, 1))));
    cases.add(
        new Case(
            "unused definition",
            scene(List.of(LIGHT_ON), "unused", List.of(LIGHT_OFF)),
            CommandResult.unreachableScene("unused")));
    cases.add(
        new Case(
            "definitions referenced only by unused definitions",
            scene(
                List.of(ref("a")),
                "a",
                List.of(LIGHT_ON),
                "b",
                List.of(ref("c")),
                "c",
                List.of(LIGHT_OFF)),
            CommandResult.unreachableScene("b")));
    cases.add(
        new Case(
            "a reference error precedes a later invalid command",
            scene(List.of(ref("missing"), badTemperature)),
            CommandResult.unknownScene("missing").atPath(List.of(0))));
    cases.add(
        new Case(
            "an invalid command precedes a later reference error",
            scene(List.of(new InvalidSceneEntry(ErrorCode.INVALID_COMMAND, "unknown"), ref("x"))),
            CommandResult.failure(ErrorCode.INVALID_COMMAND, "unknown").atPath(List.of(0))));
    cases.add(
        new Case(
            "traversal errors precede inventory errors",
            scene(List.of(new SetLightPower("missing", true), ref("missing"))),
            CommandResult.unknownScene("missing").atPath(List.of(1))));
    cases.add(
        new Case(
            "reachability precedes inventory errors",
            scene(List.of(new SetLightPower("missing", true)), "unused", List.of(LIGHT_ON)),
            CommandResult.unreachableScene("unused")));
    cases.add(
        new Case(
            "the expansion limit precedes inventory errors",
            scene(
                List.of(new SetLightPower("missing", true), ref("fifty")),
                "fifty",
                Collections.nCopies(50, LIGHT_ON)),
            CommandResult.sceneSizeExceeded().atPath(List.of(1, 49))));
    return cases;
  }

  private static NestedSceneRequest scene(List<SceneEntry> root, Object... definitions) {
    Map<String, List<SceneEntry>> scenes = new LinkedHashMap<>();
    for (int index = 0; index < definitions.length; index += 2) {
      @SuppressWarnings("unchecked")
      List<SceneEntry> entries = (List<SceneEntry>) definitions[index + 1];
      scenes.put((String) definitions[index], entries);
    }
    return new NestedSceneRequest("nested", root, scenes);
  }

  private static SceneEntry ref(String id) {
    return new SceneReference(id);
  }

  private static SceneEntry living(double temperature) {
    return new SetTargetTemperature("living-thermostat", temperature);
  }

  private static SceneEntry bedroom(double temperature) {
    return new SetTargetTemperature("bedroom-thermostat", temperature);
  }

  private static double temperature(SmartHome home, String id) {
    return home.getSnapshot().devices().stream()
        .filter(device -> device.id().equals(id))
        .map(ThermostatSnapshot.class::cast)
        .findFirst()
        .orElseThrow()
        .targetTemperature();
  }

  private static boolean light(SmartHome home) {
    return home.getSnapshot().devices().stream()
        .filter(LightSnapshot.class::isInstance)
        .map(LightSnapshot.class::cast)
        .findFirst()
        .orElseThrow()
        .power();
  }
}
