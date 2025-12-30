package sourceconnector.domain.log

import offsetmanager.domain.file.FileKey

/**
 * LogMetadata has a metadata of [Log]
 */
interface LogMetadata {
  /**
   * Identifier of file, object, etc. in the storage system.
   * @return the key path of the log in the storage system.
   */
  val key: FileKey
  val offset: Long
  /**
   * Return the offset of the current input log; could be `-1` if it is not available.
   * @return the offset of the log in the file, object, etc
   */
  companion object {
    @JvmField
    val EMPTY: LogMetadata = EmptyLogMetadata.INSTANCE
  }
}
