package sourceconnector.domain.log

import offsetmanager.domain.file.FileKey

enum class EmptyLogMetadata(
  private val emptyFileKey: FileKey = EmptyFileKey(),
  private val emptyOffset: Long = -1
) : LogMetadata {
  INSTANCE;

  override fun key(): FileKey {
    return emptyFileKey
  }

  override fun offset(): Long {
    return emptyOffset
  }
}
