package ro.upb.sasps.smarthome.contracts;

import java.util.Objects;

/** Outcome of a nested scene: an error, or the number of executed expanded commands. */
public record SceneResult(CommandResult result, int executedCommands) {
  public SceneResult {
    Objects.requireNonNull(result, "result");
    boolean validCount =
        result.ok()
            ? executedCommands >= 1 && executedCommands <= ContractChecks.MAX_SCENE_COMMANDS
            : executedCommands == 0;
    if (!validCount) {
      throw new IllegalArgumentException("A success executes 1–50 commands and an error none");
    }
  }

  public static SceneResult success(int executedCommands) {
    return new SceneResult(CommandResult.success(), executedCommands);
  }

  public static SceneResult failure(CommandResult result) {
    return new SceneResult(result, 0);
  }
}
