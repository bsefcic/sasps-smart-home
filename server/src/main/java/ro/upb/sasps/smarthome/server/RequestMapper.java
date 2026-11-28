package ro.upb.sasps.smarthome.server;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import ro.upb.sasps.smarthome.contracts.ContractChecks;
import ro.upb.sasps.smarthome.contracts.ErrorCode;
import ro.upb.sasps.smarthome.contracts.HomeCommand;
import ro.upb.sasps.smarthome.contracts.InvalidSceneEntry;
import ro.upb.sasps.smarthome.contracts.NestedSceneRequest;
import ro.upb.sasps.smarthome.contracts.SceneEntry;
import ro.upb.sasps.smarthome.contracts.SceneReference;
import ro.upb.sasps.smarthome.contracts.SceneRequest;
import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SetTargetTemperature;

/** HTTP shape/type validation only; no inventory lookup or scene execution. */
@Component
public final class RequestMapper {
  public HomeCommand command(JsonNode node) {
    requireObject(node);
    String type = text(node, "type");
    Set<String> fields =
        switch (type) {
          case "set-light-power" -> Set.of("type", "deviceId", "power");
          case "set-target-temperature" -> Set.of("type", "deviceId", "temperature");
          default ->
              throw new ApiException(ErrorCode.INVALID_COMMAND, "Unknown command type: " + type);
        };
    exactFields(node, fields);
    String id = text(node, "deviceId");
    try {
      ContractChecks.requireDeviceId(id);
    } catch (IllegalArgumentException exception) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, exception.getMessage());
    }
    if (type.equals("set-light-power")) {
      if (!node.get("power").isBoolean()) {
        throw new ApiException(ErrorCode.INVALID_REQUEST, "power must be a boolean");
      }
      return new SetLightPower(id, node.get("power").booleanValue());
    }
    JsonNode value = node.get("temperature");
    if (!value.isNumber()) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, "temperature must be a number");
    }
    try {
      return new SetTargetTemperature(id, value.doubleValue());
    } catch (IllegalArgumentException exception) {
      throw new ApiException(ErrorCode.INVALID_TEMPERATURE, exception.getMessage());
    }
  }

  public MotionReading motionReading(JsonNode node) {
    requireObject(node);
    exactFields(node, Set.of("sensorId", "motion"));
    String id = text(node, "sensorId");
    try {
      ContractChecks.requireDeviceId(id);
    } catch (IllegalArgumentException exception) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, exception.getMessage());
    }
    if (!node.get("motion").isBoolean()) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, "motion must be a boolean");
    }
    return new MotionReading(id, node.get("motion").booleanValue());
  }

  public SceneRequest scene(JsonNode node) {
    requireObject(node);
    exactFields(node, Set.of("name", "commands"));
    String name = text(node, "name");
    JsonNode list = node.get("commands");
    if (name.isBlank()
        || name.length() > ContractChecks.MAX_SCENE_NAME_LENGTH
        || !list.isArray()
        || list.isEmpty()
        || list.size() > ContractChecks.MAX_SCENE_COMMANDS) {
      throw new ApiException(
          ErrorCode.INVALID_SCENE, "A scene needs a nonblank name (max 64) and 1–50 commands");
    }
    List<HomeCommand> commands = new ArrayList<>();
    for (int index = 0; index < list.size(); index++) {
      try {
        commands.add(command(list.get(index)));
      } catch (ApiException exception) {
        throw exception.atCommand(index);
      }
    }
    return new SceneRequest(name, commands);
  }

  public boolean isNestedScene(JsonNode node) {
    return node != null && node.isObject() && node.has("scenes");
  }

  public NestedSceneRequest nestedScene(JsonNode node) {
    requireObject(node);
    exactFields(node, Set.of("name", "commands", "scenes"));
    String name = text(node, "name");
    JsonNode registry = node.get("scenes");
    if (!registry.isObject()) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, "scenes must be an object");
    }
    if (name.isBlank() || name.length() > ContractChecks.MAX_SCENE_NAME_LENGTH) {
      throw new ApiException(ErrorCode.INVALID_SCENE, "A scene needs a nonblank name (max 64)");
    }
    if (registry.size() > ContractChecks.MAX_SCENE_DEFINITIONS) {
      throw new ApiException(ErrorCode.INVALID_SCENE, "scenes can define at most 50 scenes");
    }
    List<SceneEntry> commands = entries(node.get("commands"));
    Map<String, List<SceneEntry>> scenes = new LinkedHashMap<>();
    for (Map.Entry<String, JsonNode> definition : registry.properties()) {
      try {
        ContractChecks.requireSceneId(definition.getKey());
      } catch (IllegalArgumentException exception) {
        throw new ApiException(ErrorCode.INVALID_REQUEST, exception.getMessage());
      }
      requireObject(definition.getValue());
      exactFields(definition.getValue(), Set.of("commands"));
      scenes.put(definition.getKey(), entries(definition.getValue().get("commands")));
    }
    return new NestedSceneRequest(name, commands, scenes);
  }

  private List<SceneEntry> entries(JsonNode list) {
    if (!list.isArray() || list.isEmpty() || list.size() > ContractChecks.MAX_SCENE_COMMANDS) {
      throw new ApiException(ErrorCode.INVALID_SCENE, "Every scene list needs 1–50 commands");
    }
    List<SceneEntry> entries = new ArrayList<>();
    list.forEach(element -> entries.add(entry(element)));
    return entries;
  }

  private SceneEntry entry(JsonNode node) {
    try {
      if (node.isObject() && "scene-ref".equals(node.path("type").textValue())) {
        exactFields(node, Set.of("type", "sceneId"));
        String id = text(node, "sceneId");
        try {
          return new SceneReference(id);
        } catch (IllegalArgumentException exception) {
          throw new ApiException(ErrorCode.INVALID_REQUEST, exception.getMessage());
        }
      }
      return command(node);
    } catch (ApiException exception) {
      return new InvalidSceneEntry(exception.code(), exception.getMessage());
    }
  }

  private static void requireObject(JsonNode node) {
    if (node == null || !node.isObject()) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, "Request must be a JSON object");
    }
  }

  private static String text(JsonNode node, String field) {
    if (!node.hasNonNull(field) || !node.get(field).isTextual()) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, field + " must be a string");
    }
    return node.get(field).textValue();
  }

  private static void exactFields(JsonNode node, Set<String> expected) {
    if (node.size() != expected.size() || !expected.stream().allMatch(node::hasNonNull)) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, "Missing, null or unexpected fields");
    }
    node.fieldNames()
        .forEachRemaining(
            field -> {
              if (!expected.contains(field)) {
                throw new ApiException(ErrorCode.INVALID_REQUEST, "Unexpected field: " + field);
              }
            });
  }
}
