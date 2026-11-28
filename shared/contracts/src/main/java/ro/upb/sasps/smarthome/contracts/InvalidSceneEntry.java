package ro.upb.sasps.smarthome.contracts;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * An element whose form or values the transport rejected. Engines report it, with its commandPath,
 * only when traversal reaches it, so that earlier reference and limit errors keep precedence.
 */
public record InvalidSceneEntry(ErrorCode code, String message) implements SceneEntry {
  private static final Set<ErrorCode> CODES =
      EnumSet.of(
          ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_COMMAND, ErrorCode.INVALID_TEMPERATURE);

  public InvalidSceneEntry {
    if (!CODES.contains(code)) {
      throw new IllegalArgumentException("code must describe an invalid scene element");
    }
    Objects.requireNonNull(message, "message");
  }

  public CommandResult toResult() {
    return CommandResult.failure(code, message);
  }
}
