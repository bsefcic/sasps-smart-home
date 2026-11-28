package ro.upb.sasps.smarthome.contracts;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The extended scene form: a root list plus local definitions indexed by ID. Only list and registry
 * bounds are checked here; references, cycles, depth and expansion are engine checks.
 */
public record NestedSceneRequest(
    String name, List<SceneEntry> commands, Map<String, List<SceneEntry>> scenes) {
  public NestedSceneRequest {
    ContractChecks.requireSceneName(name);
    ContractChecks.requireSceneList(commands);
    if (scenes == null || scenes.size() > ContractChecks.MAX_SCENE_DEFINITIONS) {
      throw new IllegalArgumentException("scenes must contain 0–50 definitions");
    }
    commands = List.copyOf(commands);
    Map<String, List<SceneEntry>> copy = new LinkedHashMap<>();
    scenes.forEach(
        (id, entries) -> {
          ContractChecks.requireSceneId(id);
          ContractChecks.requireSceneList(entries);
          copy.put(id, List.copyOf(entries));
        });
    scenes = Collections.unmodifiableMap(copy);
  }
}
