package ro.upb.sasps.smarthome.contracts;

public interface SmartHome {
  CommandResult execute(HomeCommand command);

  HomeSnapshot getSnapshot();

  /** Stage boundary: implemented separately in Sprint 1 story #6. */
  default CommandResult executeScene(SceneRequest scene) {
    return CommandResult.failure(
        ErrorCode.NOT_IMPLEMENTED, "Scene execution is not implemented yet.");
  }
}
