package ro.upb.sasps.smarthome.contracts;

import java.util.List;

public record SceneRequest(String name, List<HomeCommand> commands) {
  public SceneRequest {
    ContractChecks.requireSceneName(name);
    ContractChecks.requireSceneList(commands);
    commands = List.copyOf(commands);
  }
}
