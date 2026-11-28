package ro.upb.sasps.smarthome.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.ContractChecks;
import ro.upb.sasps.smarthome.contracts.HomeCommand;
import ro.upb.sasps.smarthome.contracts.InvalidSceneEntry;
import ro.upb.sasps.smarthome.contracts.SceneEntry;
import ro.upb.sasps.smarthome.contracts.SceneReference;

/**
 * Builds the Composite tree of one nested request: each reference becomes its own SceneCommand, so
 * a reused definition is compiled again at every position and depth. Building has no effects.
 */
final class SceneCompiler {
  private final Map<String, List<SceneEntry>> scenes;
  private final Function<HomeCommand, CommandResult> validator;
  private final Function<HomeCommand, Command> factory;
  private final Set<String> active = new HashSet<>();
  private final Set<String> reached = new HashSet<>();
  private int commandCount;
  private CommandResult inventoryError;
  private SceneCommand scene;
  private boolean used;

  SceneCompiler(
      Map<String, List<SceneEntry>> scenes,
      Function<HomeCommand, CommandResult> validator,
      Function<HomeCommand, Command> factory) {
    this.scenes = Objects.requireNonNull(scenes, "scenes");
    this.validator = Objects.requireNonNull(validator, "validator");
    this.factory = Objects.requireNonNull(factory, "factory");
  }

  /** Returns the first error in contract order, or success once scene() is ready to execute. */
  CommandResult compile(List<SceneEntry> root) {
    if (used) {
      throw new IllegalStateException("A compiler handles one request");
    }
    used = true;
    List<Command> children = new ArrayList<>();
    CommandResult result = compile(root, List.of(), children);
    if (result.ok()) {
      result = firstUnreachable();
    }
    if (result.ok() && inventoryError != null) {
      result = inventoryError;
    }
    if (result.ok()) {
      scene = new SceneCommand(children);
    }
    return result;
  }

  SceneCommand scene() {
    if (scene == null) {
      throw new IllegalStateException("No scene was compiled successfully");
    }
    return scene;
  }

  int commandCount() {
    return commandCount;
  }

  private CommandResult compile(
      List<SceneEntry> entries, List<Integer> path, List<Command> children) {
    for (int index = 0; index < entries.size(); index++) {
      List<Integer> entryPath = new ArrayList<>(path);
      entryPath.add(index);
      CommandResult result =
          switch (entries.get(index)) {
            case InvalidSceneEntry invalid -> invalid.toResult().atPath(entryPath);
            case HomeCommand command -> leaf(command, entryPath, children);
            case SceneReference reference -> subscene(reference.sceneId(), entryPath, children);
          };
      if (!result.ok()) {
        return result;
      }
    }
    return CommandResult.success();
  }

  /** Inventory errors are kept until traversal and reachability pass, as the contract orders. */
  private CommandResult leaf(HomeCommand command, List<Integer> path, List<Command> children) {
    if (commandCount == ContractChecks.MAX_SCENE_COMMANDS) {
      return CommandResult.sceneSizeExceeded().atPath(path);
    }
    commandCount++;
    CommandResult valid = validator.apply(command);
    if (valid.ok()) {
      children.add(factory.apply(command));
    } else if (inventoryError == null) {
      inventoryError = valid.atPath(path);
    }
    return CommandResult.success();
  }

  private CommandResult subscene(String id, List<Integer> path, List<Command> children) {
    if (!scenes.containsKey(id)) {
      return CommandResult.unknownScene(id).atPath(path);
    }
    if (active.contains(id)) {
      return CommandResult.sceneCycle(id).atPath(path);
    }
    if (path.size() == ContractChecks.MAX_SCENE_DEPTH) {
      return CommandResult.sceneDepthExceeded().atPath(path);
    }
    reached.add(id);
    active.add(id);
    List<Command> subscene = new ArrayList<>();
    CommandResult result = compile(scenes.get(id), path, subscene);
    active.remove(id);
    if (result.ok()) {
      children.add(new SceneCommand(subscene));
    }
    return result;
  }

  private CommandResult firstUnreachable() {
    for (String id : scenes.keySet()) {
      if (!reached.contains(id)) {
        return CommandResult.unreachableScene(id);
      }
    }
    return CommandResult.success();
  }
}
