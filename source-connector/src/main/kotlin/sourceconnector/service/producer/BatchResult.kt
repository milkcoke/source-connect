package sourceconnector.service.producer

enum class BatchResult {
  SUCCESS,
  FAIL;

  val isFailure: Boolean get() = this != SUCCESS
}
