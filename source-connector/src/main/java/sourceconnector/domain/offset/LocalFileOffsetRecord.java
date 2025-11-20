package sourceconnector.domain.offset;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;

public record LocalFileOffsetRecord(
  FileKey key,
  long offset
) implements OffsetRecord {
}
