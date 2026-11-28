package ro.upb.sasps.smarthome.server;

import java.util.List;
import ro.upb.sasps.smarthome.contracts.ErrorCode;

final class ApiException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final ErrorCode code;
  private final Integer commandIndex;
  private final transient List<Integer> commandPath;

  ApiException(ErrorCode code, String message) {
    this(code, message, null, null);
  }

  ApiException(ErrorCode code, String message, Integer commandIndex, List<Integer> commandPath) {
    super(message);
    this.code = code;
    this.commandIndex = commandIndex;
    this.commandPath = commandPath == null ? null : List.copyOf(commandPath);
  }

  ErrorCode code() {
    return code;
  }

  Integer commandIndex() {
    return commandIndex;
  }

  List<Integer> commandPath() {
    return commandPath;
  }

  ApiException atCommand(int index) {
    return new ApiException(code, getMessage(), index, null);
  }
}
