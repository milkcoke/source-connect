package offsetmanager.exception;


import lombok.Getter;

@Getter
public class OffsetNotFoundException extends RuntimeException {
  private final String key;
  /**
   * Construct offsetmanager.exception avoiding stack trace for performance reasons.
   * @param key object key for which the offset was not found
   */
  public OffsetNotFoundException(String key) {
    super("Offset not found for key: " + key, null, false, false);
    this.key = key;
  }
}
