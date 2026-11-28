package ro.upb.sasps.smarthome.patterns;

import java.util.List;

/**
 * Composite: executes its children in their supplied order. A child is a leaf command or another
 * SceneCommand, so a nested scene runs depth-first through the same execute operation. The engine
 * must validate the entire scene before constructing and executing this command; execution does not
 * provide rollback.
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
