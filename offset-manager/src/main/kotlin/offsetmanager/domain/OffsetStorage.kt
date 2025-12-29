package offsetmanager.domain

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.OffsetRecord
import java.util.*

/**
 * High-level interface for offset storage operations <br></br>
 * OffsetStorage should store latest OffsetRecord identified by FileKey
 */
interface OffsetStorage {
  fun find(key: FileKey): Optional<OffsetRecord>
  fun upsert(key: FileKey, record: OffsetRecord)
  fun remove(key: FileKey)
  fun clear()
}
