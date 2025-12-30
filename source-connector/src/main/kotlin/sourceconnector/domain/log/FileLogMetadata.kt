package sourceconnector.domain.log

import offsetmanager.domain.file.FileKey

/**
 *
 * @param key the file path log is saved
 * @param offset offset in the log file
 */
data class FileLogMetadata(
  override val key: FileKey,
  override val offset: Long
) : LogMetadata {
}
