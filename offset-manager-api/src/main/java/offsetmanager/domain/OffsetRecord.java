package offsetmanager.domain;


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
  String key();

  /**
   * The offset value <br>
   * Refer to the {@link offsetmanager.domain.OffsetStatus} defines special offset
   */
  long offset();
}
