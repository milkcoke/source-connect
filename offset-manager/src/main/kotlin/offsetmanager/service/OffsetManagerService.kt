package offsetmanager.service

import offsetmanager.api.v1.dto.LastOffsetRecordBatchResponse
import offsetmanager.api.v1.dto.LastOffsetRecordResponse
import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.factory.FileKeyParser
import offsetmanager.domain.file.factory.FileKeyParser.Companion.parse
import offsetmanager.domain.offset.OffsetRecord
import offsetmanager.exception.OffsetNotFoundException
import offsetmanager.manager.OffsetManager
import org.springframework.stereotype.Service
import java.util.*

@Service
class OffsetManagerService(
  private val offsetManager: OffsetManager
) {
  fun readLastOffset(key: String): LastOffsetRecordResponse {
    val fileKey = parse(key)

    val lastOffsetRecord: Optional<OffsetRecord> = offsetManager.findLatestOffsetRecord(fileKey)
    if (lastOffsetRecord.isEmpty) {
      throw OffsetNotFoundException(key)
    }
    return LastOffsetRecordResponse.from(lastOffsetRecord.get())
  }

  fun readLastOffsets(keys: List<String>): LastOffsetRecordBatchResponse {
    val fileKeys = keys.stream()
      .map<FileKey> { key -> FileKeyParser.parse(key) }
      .toList()

    val offsetRecordList: List<OffsetRecord> = this.offsetManager.findLatestOffsetRecords(fileKeys)
    return LastOffsetRecordBatchResponse.from(offsetRecordList)
  }
}
