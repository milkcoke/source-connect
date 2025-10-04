package sourceconnector.domain.log;

/**
 *
 * @param filePath the file path log is saved
 * @param offset offset in the log file
 */
public record FileLogMetadata(
  String filePath,
  long offset
) implements LogMetadata {
  @Override
  public String key() {
    return filePath;
  }
}
