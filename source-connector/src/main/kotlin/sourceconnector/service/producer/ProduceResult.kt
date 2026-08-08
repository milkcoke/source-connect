package sourceconnector.service.producer

enum class ProduceResult {
  SUCCESS,
  FAIL;

  val isFailure: Boolean get() = this != SUCCESS
}
