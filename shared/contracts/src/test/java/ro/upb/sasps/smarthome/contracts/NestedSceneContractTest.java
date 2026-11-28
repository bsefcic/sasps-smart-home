package ro.upb.sasps.smarthome.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class NestedSceneContractTest {
  private static final SceneEntry LIGHT_ON = new SetLightPower("living-light", true);

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "Warm", "bad id", "1warm", "warm ", "../warm"})
  void referencesUseTheDeviceIdSyntax(String id) {
    assertThrows(IllegalArgumentException.class, () -> new SceneReference(id));
    assertThrows(
        IllegalArgumentException.class,
        () -> new NestedSceneRequest("evening", List.of(LIGHT_ON), definitions(id)));
  }

  @Test
  void referenceAndDefinitionIdsHaveA64CharacterLimit() {
    String longest = "a".repeat(64);
    assertEquals(longest, new SceneReference(longest).sceneId());
    assertThrows(IllegalArgumentException.class, () -> new SceneReference(longest + "a"));
  }

  @Test
  void requestsKeepDefinitionOrderAndCopyEveryList() {
    List<SceneEntry> root = new ArrayList<>(List.of(new SceneReference("warm")));
    List<SceneEntry> warm = new ArrayList<>(List.of(LIGHT_ON));
    Map<String, List<SceneEntry>> scenes = new LinkedHashMap<>();
    scenes.put("warm", warm);
    scenes.put("cold", List.of(new SetTargetTemperature("living-thermostat", 18)));
    NestedSceneRequest request = new NestedSceneRequest("evening", root, scenes);
    root.clear();
    warm.clear();
    scenes.clear();

    assertEquals(List.of(new SceneReference("warm")), request.commands());
    assertEquals(List.of("warm", "cold"), List.copyOf(request.scenes().keySet()));
    assertEquals(List.of(LIGHT_ON), request.scenes().get("warm"));
    assertThrows(UnsupportedOperationException.class, () -> request.commands().clear());
    assertThrows(UnsupportedOperationException.class, () -> request.scenes().clear());
    assertThrows(UnsupportedOperationException.class, () -> request.scenes().get("warm").clear());
  }

  @Test
  void everyListHasOneToFiftyNonnullEntriesAndAtMostFiftyDefinitions() {
    assertThrows(
        IllegalArgumentException.class, () -> new NestedSceneRequest("e", List.of(), Map.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new NestedSceneRequest("e", Collections.nCopies(51, LIGHT_ON), Map.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new NestedSceneRequest("e", List.of(LIGHT_ON), Map.of("warm", List.of())));
    List<SceneEntry> withNull = new ArrayList<>(List.of(LIGHT_ON));
    withNull.add(null);
    assertThrows(
        IllegalArgumentException.class,
        () -> new NestedSceneRequest("e", List.of(LIGHT_ON), Map.of("warm", withNull)));
    assertThrows(
        IllegalArgumentException.class, () -> new NestedSceneRequest("e", List.of(LIGHT_ON), null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new NestedSceneRequest(" ", List.of(LIGHT_ON), Map.of()));

    Map<String, List<SceneEntry>> fifty = new HashMap<>();
    for (int index = 0; index < 50; index++) {
      fifty.put("scene-" + index, List.of(LIGHT_ON));
    }
    assertEquals(
        50, new NestedSceneRequest("e", Collections.nCopies(50, LIGHT_ON), fifty).scenes().size());
    fifty.put("scene-50", List.of(LIGHT_ON));
    assertThrows(
        IllegalArgumentException.class,
        () -> new NestedSceneRequest("e", List.of(LIGHT_ON), fifty));
  }

  @Test
  void commandPathIsCopiedAndExcludesCommandIndex() {
    List<Integer> path = new ArrayList<>(List.of(1, 0));
    CommandResult result = CommandResult.sceneCycle("loop").atPath(path);
    path.clear();

    assertEquals(List.of(1, 0), result.commandPath());
    assertNull(result.commandIndex());
    assertNull(CommandResult.sceneCycle("loop").atCommand(1).commandPath());
    assertThrows(IllegalArgumentException.class, () -> CommandResult.success().atPath(List.of(0)));
    assertThrows(
        IllegalArgumentException.class, () -> CommandResult.unknownScene("a").atPath(List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> CommandResult.unknownScene("a").atPath(List.of(0, 0, 0, 0, 0, 0)));
    assertThrows(
        IllegalArgumentException.class,
        () -> CommandResult.unknownScene("a").atPath(List.of(0, -1)));
  }

  @ParameterizedTest
  @EnumSource(
      value = ErrorCode.class,
      names = {"INVALID_REQUEST", "INVALID_COMMAND", "INVALID_TEMPERATURE"})
  void invalidEntriesCarryOnlyElementErrors(ErrorCode code) {
    assertEquals(CommandResult.failure(code, "bad"), new InvalidSceneEntry(code, "bad").toResult());
  }

  @ParameterizedTest
  @EnumSource(
      value = ErrorCode.class,
      names = {"INVALID_REQUEST", "INVALID_COMMAND", "INVALID_TEMPERATURE"},
      mode = EnumSource.Mode.EXCLUDE)
  void invalidEntriesRejectOtherCodes(ErrorCode code) {
    assertThrows(IllegalArgumentException.class, () -> new InvalidSceneEntry(code, "bad"));
  }

  @Test
  void sceneResultsCountOneToFiftyCommandsOnlyOnSuccess() {
    assertEquals(50, SceneResult.success(50).executedCommands());
    assertEquals(0, SceneResult.failure(CommandResult.unknownScene("a")).executedCommands());
    assertThrows(IllegalArgumentException.class, () -> SceneResult.success(0));
    assertThrows(IllegalArgumentException.class, () -> SceneResult.success(51));
    assertThrows(
        IllegalArgumentException.class, () -> SceneResult.failure(CommandResult.success()));
  }

  private static Map<String, List<SceneEntry>> definitions(String id) {
    Map<String, List<SceneEntry>> scenes = new HashMap<>();
    scenes.put(id, List.of(LIGHT_ON));
    return scenes;
  }
}
