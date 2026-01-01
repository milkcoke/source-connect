package offsetmanager.domain

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.OffsetRecord
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryOffsetStorage(
  private val offsetMap: MutableMap<FileKey, OffsetRecord> = ConcurrentHashMap()
) : OffsetStorage {

  override fun find(key: FileKey): OffsetRecord? {
    return this.offsetMap[key]
  }

  override fun upsert(key: FileKey, record: OffsetRecord) {
    this.offsetMap[key] = record
  }

  override fun remove(key: FileKey) {
    this.offsetMap.remove(key)
  }

  override fun clear() {
    this.offsetMap.clear()
  }
}
