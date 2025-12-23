package offsetmanager.api.v1.dto;

import offsetmanager.domain.offset.OffsetRecord;

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
