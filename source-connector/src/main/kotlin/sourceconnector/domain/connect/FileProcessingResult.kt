package sourceconnector.domain.connect

class FileProcessingResult(
  val totalCount: Int
) {
  var successCount: Int = 0
  var failureCount: Int = 0
  var skippedCount: Int = 0

  fun addSuccessCount() {
    successCount++
  }

  fun addFailureCount() {
    failureCount++
  }

  fun addSkippedCount() {
    skippedCount++
  }
}
