package offsetmanager.exception;

public class OffsetManagerNotReadyException extends RuntimeException {
  public OffsetManagerNotReadyException() {
    super("Offset manager is not ready", null, false, false);
  }
}
