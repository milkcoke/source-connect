package offsetmanager.domain.offset

import offsetmanager.domain.file.FileKey
/**
 * Domain interface used by OffsetManager and SourceConnector Producer. <br></br>
 * Consists of object unique identifier and offset. <br></br>
 * Stored in the Offset topic partition.
 */
interface OffsetRecord {
  /**
   * The unique key representing the source object <br></br>
   * e.g., S3 bucket and object key, local file path
   */
  fun key(): FileKey

  /**
   * The offset value <br></br>
   * Refer to the [OffsetStatus] defines special offset
   */
  fun offset(): Long
}
