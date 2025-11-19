package sourceconnector.domain.offset;

import offsetmanager.domain.offset.OffsetRecord;

public record LocalFileOffsetRecord(
  String key,
  long offset
) implements OffsetRecord {
}
