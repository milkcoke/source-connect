package sourceconnector.domain.log;

import offsetmanager.domain.file.FileKey;

/**
 *
 * @param fileKey the file path log is saved
 * @param offset offset in the log file
 */
public record FileLogMetadata(
  FileKey fileKey,
  long offset
) implements LogMetadata {
  @Override
  public String key() {
    return fileKey.get();
  }
}
