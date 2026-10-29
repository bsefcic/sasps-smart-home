package ro.upb.sasps.smarthome.server;

import org.springframework.stereotype.Service;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.HomeCommand;
import ro.upb.sasps.smarthome.contracts.HomeSnapshot;
import ro.upb.sasps.smarthome.contracts.SceneRequest;
import ro.upb.sasps.smarthome.contracts.SmartHome;

/** Neutral synchronization: one home per process, same policy for both variants. */
@Service
public final class HomeSession {
  private final SmartHome home;

  public HomeSession(SmartHome home) {
    this.home = home;
  }

  public synchronized HomeSnapshot snapshot() {
    return home.getSnapshot();
  }

  public synchronized ExecutionResponse execute(HomeCommand command) {
    requireSuccess(home.execute(command));
    return new ExecutionResponse(true, 1, home.getSnapshot());
  }

  public synchronized ExecutionResponse executeScene(SceneRequest scene) {
    requireSuccess(home.executeScene(scene));
    return new ExecutionResponse(true, scene.commands().size(), home.getSnapshot());
  }

  private static void requireSuccess(CommandResult result) {
    if (!result.ok()) {
      throw new ApiException(result.code(), result.message(), result.commandIndex());
    }
  }
}
