package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey

interface OffsetRecordService {
  fun offsetMap(fileKeys: List<FileKey>): Map<FileKey, Long>
}
