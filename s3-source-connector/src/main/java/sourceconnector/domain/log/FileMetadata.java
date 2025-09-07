package sourceconnector.domain.log;

/**
 *
 * @param filePath the file path log is saved
 * @param offset offset in the log file
 */
public record FileMetadata (
  String filePath,
  long offset
) implements Metadata {
}
