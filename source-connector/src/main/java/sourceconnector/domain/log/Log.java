package sourceconnector.domain.log;

/**
 * Log Should have the payload and metadata
 */
public interface Log {
  /**
   * Get the payload of the log
   *
   * @return String payload
   */
  String get();
  LogMetadata getMetadata();
}
