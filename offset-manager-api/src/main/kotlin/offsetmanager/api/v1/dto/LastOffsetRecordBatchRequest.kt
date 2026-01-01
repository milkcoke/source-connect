package offsetmanager.api.v1.dto

data class LastOffsetRecordBatchRequest(
  // TODO: enforce not empty list
  val keys: List<String>
)
