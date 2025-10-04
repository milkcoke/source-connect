package sourceconnector.domain.log;

/**
 * LogMetadata has a metadata of {@link Log}
 */
public interface LogMetadata {
  LogMetadata EMPTY = EmptyLogMetadata.INSTANCE;
  /**
   * Identifier of file, object, etc. in the storage system.
   * @return the key path of the log in the storage system.
   */
  String key();
  /**
   * Return the offset of the current input log; could be {@code -1} if it is not available.
   * @return the offset of the log in the file, object, etc
   */
  long offset();
}
