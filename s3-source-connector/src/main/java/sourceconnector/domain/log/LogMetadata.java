package sourceconnector.domain.log;

/**
 * Log should have metadata to identify its origin.
 */
public interface LogMetadata {
  /**
   * Key of the log file in the storage system (e.g., S3 key, LocalFile Path, Azure Blog key).
   */
  String key();
  /**
   * Offset of the log in the file, object, etc..
   */
  long offset();
}
