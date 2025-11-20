package sourceconnector.domain.offset;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;

public record S3OffsetRecord(
  FileKey key,
  long offset
) implements OffsetRecord {
}
