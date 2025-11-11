package sourceconnector.domain.file;

/**
 * Used as File identifier. <br>
 * This is also used in OffsetRecord key to identify the file being processed.
 */
public interface FileKey {
  String get();
}
