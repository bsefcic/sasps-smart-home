package ro.upb.sasps.smarthome.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ro.upb.sasps.smarthome.contracts.ErrorCode;

@RestControllerAdvice
public final class ApiErrorHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ApiErrorHandler.class);

  public record ErrorDetail(ErrorCode code, String message, Integer commandIndex) {}

  public record ErrorResponse(boolean ok, ErrorDetail error) {}

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> api(ApiException exception) {
    int status =
        switch (exception.code()) {
          case UNKNOWN_DEVICE, NOT_FOUND -> 404;
          case INVALID_TEMPERATURE, INCOMPATIBLE_DEVICE -> 422;
          case NOT_IMPLEMENTED -> 501;
          case INTERNAL_ERROR -> 500;
          default -> 400;
        };
    return error(status, exception.code(), exception.getMessage(), exception.commandIndex());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> malformed() {
    return error(400, ErrorCode.INVALID_REQUEST, "Request body must be valid JSON.", null);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> mediaType() {
    return error(415, ErrorCode.INVALID_REQUEST, "Content-Type must be application/json.", null);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> method() {
    return error(405, ErrorCode.INVALID_REQUEST, "HTTP method not allowed.", null);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> notFound() {
    return error(404, ErrorCode.NOT_FOUND, "Endpoint not found.", null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> unexpected(Exception exception) {
    LOG.error("Unexpected request failure", exception);
    return error(500, ErrorCode.INTERNAL_ERROR, "Unexpected server error.", null);
  }

  private static ResponseEntity<ErrorResponse> error(
      int status, ErrorCode code, String message, Integer index) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(false, new ErrorDetail(code, message, index)));
  }
}
