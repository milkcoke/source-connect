package offsetmanager.domain.file;

/**
 * Used as File identifier. <br>
 * This is also used in OffsetRecord key to identify the file being processed.
 */
public interface FileKey extends Comparable<FileKey> {
  String get();

  /**
   * Compare based on the String value of the FileKey
   */
  @Override
  default int compareTo(FileKey other) {
    return this.get().compareTo(other.get());
  }
}
