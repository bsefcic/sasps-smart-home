package ro.upb.sasps.smarthome.server;

import ro.upb.sasps.smarthome.contracts.ErrorCode;

final class ApiException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  private final ErrorCode code;
  private final Integer commandIndex;

  ApiException(ErrorCode code, String message) {
    this(code, message, null);
  }

  ApiException(ErrorCode code, String message, Integer commandIndex) {
    super(message);
    this.code = code;
    this.commandIndex = commandIndex;
  }

  ErrorCode code() {
    return code;
  }

  Integer commandIndex() {
    return commandIndex;
  }

  ApiException atCommand(int index) {
    return new ApiException(code, getMessage(), index);
  }
}
