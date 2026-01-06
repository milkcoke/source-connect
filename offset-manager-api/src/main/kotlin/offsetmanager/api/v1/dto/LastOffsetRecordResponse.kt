package offsetmanager.api.v1.dto

import offsetmanager.domain.offset.OffsetRecord

data class LastOffsetRecordResponse(
  val key: String,
  val offset: Long
) {
  companion object {
    fun from(offsetRecord: OffsetRecord): LastOffsetRecordResponse {
      return LastOffsetRecordResponse(
        offsetRecord.key().get(),
        offsetRecord.offset()
      )
    }
  }
}
