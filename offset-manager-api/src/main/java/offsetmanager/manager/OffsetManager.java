package offsetmanager.manager;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;

import java.util.List;
import java.util.Optional;

public interface OffsetManager {
  Optional<OffsetRecord> findLatestOffsetRecord(FileKey key);
  List<OffsetRecord> findLatestOffsetRecords(List<FileKey> keys);
  void upsert(FileKey fileKey, OffsetRecord offsetRecord);
  void removeKey(FileKey key);
}
