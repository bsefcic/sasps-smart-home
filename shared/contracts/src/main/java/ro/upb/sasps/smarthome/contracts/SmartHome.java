package ro.upb.sasps.smarthome.contracts;

public interface SmartHome {
  CommandResult execute(HomeCommand command);

  HomeSnapshot getSnapshot();

  /**
   * Executes a flat scene after validating every command, without partial effects on invalid input.
   */
  default CommandResult executeScene(SceneRequest scene) {
    return CommandResult.failure(
        ErrorCode.NOT_IMPLEMENTED, "Scene execution is not implemented yet.");
  }
}
