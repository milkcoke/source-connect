package offsetmanager.manager

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.OffsetRecord
import java.util.*

interface OffsetManager {
  fun findLatestOffsetRecord(key: FileKey): Optional<OffsetRecord>
  fun findLatestOffsetRecords(keys: List<FileKey>): List<OffsetRecord>
}
