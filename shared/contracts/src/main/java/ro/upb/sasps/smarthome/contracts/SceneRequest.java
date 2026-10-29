package ro.upb.sasps.smarthome.contracts;

import java.util.List;

public record SceneRequest(String name, List<HomeCommand> commands) {
  public SceneRequest {
    if (name == null || name.isBlank() || name.length() > 64) {
      throw new IllegalArgumentException("name must be nonblank and at most 64 characters");
    }
    if (commands == null
        || commands.isEmpty()
        || commands.size() > ContractChecks.MAX_SCENE_COMMANDS
        || commands.stream().anyMatch(command -> command == null)) {
      throw new IllegalArgumentException("commands must contain 1–50 nonnull commands");
    }
    commands = List.copyOf(commands);
  }
}
