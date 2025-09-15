package sourceconnector.domain.log;

/**
 * LogMetadata has a metadata of {@link Log} identify its origin.
 */
public interface LogMetadata {
  LogMetadata EMPTY = EmptyLogMetadata.INSTANCE;
  /**
   * Return the key path of the log in the storage system (e.g., S3 key, LocalFile Path, Azure Blog key).
   * @return the key path of the log in the storage system.
   */
  String key();
  /**
   * Return the offset of the current input log; could be {@code -1} if it is not available.
   * @return the offset of the log in the file, object, etc..
   */
  long offset();
}
