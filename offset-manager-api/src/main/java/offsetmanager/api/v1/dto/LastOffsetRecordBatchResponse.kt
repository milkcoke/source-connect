package offsetmanager.api.v1.dto

import offsetmanager.domain.offset.OffsetRecord

data class LastOffsetRecordBatchResponse(
  val lastOffsetRecords: List<LastOffsetRecordResponse>
) {
  companion object {
    fun from(offsetRecords: List<OffsetRecord>): LastOffsetRecordBatchResponse {
      return LastOffsetRecordBatchResponse(
        offsetRecords.stream()
          .map<LastOffsetRecordResponse> { offsetRecord: OffsetRecord ->
            LastOffsetRecordResponse.from(
              offsetRecord
            )
          }
          .toList()
      )
    }
  }
}
