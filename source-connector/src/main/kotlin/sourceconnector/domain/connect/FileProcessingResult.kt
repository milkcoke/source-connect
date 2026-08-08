package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey

class FileProcessingResult(
  val totalCount: Int
) {
  var successCount: Int = 0
  val failedFileKeys: MutableList<FileKey> = mutableListOf()
  var skippedCount: Int = 0

  fun addSuccessCount() {
    successCount++
  }

  fun addFailure(fileKey: FileKey) {
    failedFileKeys.add(fileKey)
  }

  fun addSkippedCount() {
    skippedCount++
  }
}
