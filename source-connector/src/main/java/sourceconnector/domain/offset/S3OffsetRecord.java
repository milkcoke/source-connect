package sourceconnector.domain.offset;

import offsetmanager.domain.offset.OffsetRecord;

public record S3OffsetRecord(
  String key,
  long offset
) implements OffsetRecord {
}
