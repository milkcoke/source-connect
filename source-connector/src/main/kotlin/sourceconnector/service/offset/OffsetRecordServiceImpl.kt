package sourceconnector.service.offset

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.OffsetRecord
import sourceconnector.domain.connect.OffsetRecordService

class OffsetRecordServiceImpl(
  private val offsetRecordRepository: OffsetRecordRepository
) : OffsetRecordService {

  override fun offsetMap(fileKeys: List<FileKey>): Map<FileKey, Long> {
    val offsetRecords: List<OffsetRecord> = this.offsetRecordRepository.findLastOffsetRecords(fileKeys)
    return offsetRecords.associate { it.key() to it.offset() }
  }
}
