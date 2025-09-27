package sourceconnector.domain.offset;

import offsetmanager.domain.OffsetRecord;

public record LocalFileOffsetRecord(
  String key,
  long offset
) implements OffsetRecord {
}
