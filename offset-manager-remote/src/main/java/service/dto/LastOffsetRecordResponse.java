package service.dto;

import offsetmanager.domain.OffsetRecord;

public record LastOffsetRecordResponse(
  String key,
  long offset
) {
  public static LastOffsetRecordResponse from(OffsetRecord offsetRecord) {
    return new LastOffsetRecordResponse(
      offsetRecord.key(),
      offsetRecord.offset()
    );
  }
}
