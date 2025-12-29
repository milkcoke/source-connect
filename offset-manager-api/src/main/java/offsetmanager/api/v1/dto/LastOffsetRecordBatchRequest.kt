package offsetmanager.api.v1.dto;

import java.util.List;

public record LastOffsetRecordBatchRequest(
  List<String> keys
) {
}
