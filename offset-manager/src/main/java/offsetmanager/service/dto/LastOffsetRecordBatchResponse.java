package offsetmanager.service.dto;

import offsetmanager.domain.OffsetRecord;

import java.util.List;

public record LastOffsetRecordBatchResponse(
  List<LastOffsetRecordResponse> lastOffsetRecords
) {
  public static LastOffsetRecordBatchResponse from(List<OffsetRecord> offsetRecords) {
    return new LastOffsetRecordBatchResponse(
      offsetRecords.stream()
        .map(LastOffsetRecordResponse::from)
        .toList()
    );
  }
}
