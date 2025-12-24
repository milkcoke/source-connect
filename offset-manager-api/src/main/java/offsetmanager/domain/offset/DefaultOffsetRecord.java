package offsetmanager.domain.offset;

import offsetmanager.domain.file.FileKey;

public record DefaultOffsetRecord(
  FileKey key,
  long offset
) implements OffsetRecord {
}
