package offsetmanager.domain

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.OffsetRecord
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryOffsetStorage(
  private val offsetMap: MutableMap<FileKey, OffsetRecord> = ConcurrentHashMap()
) : OffsetStorage {

  override fun find(key: FileKey): Optional<OffsetRecord> {
    if (this.offsetMap.containsKey(key)) {
      return Optional.of(this.offsetMap[key] as OffsetRecord)
    }
    return Optional.empty()
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
