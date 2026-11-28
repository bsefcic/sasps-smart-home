package ro.upb.sasps.smarthome.contracts;

/** Reference to a definition in the same NestedSceneRequest, resolved by the engine. */
public record SceneReference(String sceneId) implements SceneEntry {
  public SceneReference {
    ContractChecks.requireSceneId(sceneId);
  }
}
