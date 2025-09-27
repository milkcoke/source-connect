package sourceconnector.domain.offset;

import offsetmanager.domain.OffsetRecord;

public record S3OffsetRecord(
  String key,
  long offset
) implements OffsetRecord {
}
