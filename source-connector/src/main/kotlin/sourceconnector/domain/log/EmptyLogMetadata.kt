package sourceconnector.domain.log

import offsetmanager.domain.file.FileKey

enum class EmptyLogMetadata(
  override val key: FileKey = EmptyFileKey(),
  override val offset: Long = -1
) : LogMetadata {
  INSTANCE;
}
