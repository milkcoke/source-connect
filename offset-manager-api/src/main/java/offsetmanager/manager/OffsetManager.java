package offsetmanager.manager;

import offsetmanager.domain.OffsetRecord;

import java.util.Optional;

public interface OffsetManager {
  Optional<OffsetRecord> findLatestOffsetRecord(String key);
  void upsert(String key, OffsetRecord offsetRecord);
  void removeKey(String key);
}
