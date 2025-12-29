package offsetmanager.domain.offset

import offsetmanager.domain.file.FileKey

data class DefaultOffsetRecord(
  val key: FileKey,
  val offset: Long
) : OffsetRecord {
  override fun key(): FileKey {
    return key
  }

  override fun offset(): Long {
    return offset
  }
}
