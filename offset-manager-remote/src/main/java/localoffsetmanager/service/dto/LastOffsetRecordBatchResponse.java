package localoffsetmanager.service.dto;

import jakarta.validation.constraints.NotEmpty;
import offsetmanager.domain.OffsetRecord;

import java.util.List;

public record LastOffsetRecordBatchResponse(
  @NotEmpty
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
