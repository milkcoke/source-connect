package offsetmanager.api.v1.dto;

import offsetmanager.domain.offset.OffsetRecord;

public record LastOffsetRecordResponse(
  String key,
  long offset
) {
  public static LastOffsetRecordResponse from(OffsetRecord offsetRecord) {
    return new LastOffsetRecordResponse(
      offsetRecord.key().get(),
      offsetRecord.offset()
    );
  }
}
