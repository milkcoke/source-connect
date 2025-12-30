package sourceconnector.service.offset

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.OffsetRecord
import java.lang.AutoCloseable
import java.util.*

interface OffsetRecordRepository : AutoCloseable {
  fun findLastOffsetRecord(key: FileKey): OffsetRecord?
  fun findLastOffsetRecords(keys: List<FileKey>): List<OffsetRecord>
}
