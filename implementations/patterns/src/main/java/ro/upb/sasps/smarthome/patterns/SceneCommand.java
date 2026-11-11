package ro.upb.sasps.smarthome.patterns;

import java.util.List;

/**
 * Executes commands in their supplied order. The engine must validate the entire scene before
 * constructing and executing this command; execution does not provide rollback.
 */
public final class SceneCommand implements Command {
  private final List<Command> commands;

  public SceneCommand(List<Command> commands) {
    this.commands = List.copyOf(commands);
  }

  @Override
  public void execute() {
    for (Command command : commands) {
      command.execute();
    }
  }
}
