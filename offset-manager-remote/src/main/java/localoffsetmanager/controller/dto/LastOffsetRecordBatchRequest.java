package localoffsetmanager.controller.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record LastOffsetRecordBatchRequest(
  @NotEmpty
  List<String> keys
) {
}
