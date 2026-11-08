package ro.upb.sasps.smarthome.patterns;

/** Thrown when a household member is not allowed to perform an operation. */
public final class AccessDeniedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public AccessDeniedException(String message) {
    super(message);
  }
}
