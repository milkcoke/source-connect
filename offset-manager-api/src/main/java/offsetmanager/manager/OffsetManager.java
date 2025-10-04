package offsetmanager.manager;

import offsetmanager.domain.OffsetRecord;

import java.util.List;
import java.util.Optional;

public interface OffsetManager {
  Optional<OffsetRecord> findLatestOffsetRecord(String key);
  List<OffsetRecord> findLatestOffsetRecords(List<String> keys);
  void upsert(String key, OffsetRecord offsetRecord);
  void removeKey(String key);
}
