package offsetmanager.domain.offset;


import offsetmanager.domain.file.FileKey;

/**
 * Domain interface used by OffsetManager and SourceConnector Producer. <br>
 * Consists of object unique identifier and offset. <br>
 * Stored in the Offset topic partition.
 */
public interface OffsetRecord {
  /**
   * The unique key representing the source object <br>
   * e.g., S3 bucket and object key, local file path
   */
  FileKey key();

  /**
   * The offset value <br>
   * Refer to the {@link OffsetStatus} defines special offset
   */
  long offset();
}
