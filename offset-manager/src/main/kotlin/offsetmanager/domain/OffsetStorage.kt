package offsetmanager.domain;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;

import java.util.Optional;

/**
 * High-level interface for offset storage operations <br>
 * OffsetStorage should store latest OffsetRecord identified by FileKey
 */
public interface OffsetStorage {
  Optional<OffsetRecord> find(FileKey key);
  void upsert(FileKey key, OffsetRecord record);
  void remove(FileKey key);
  void clear();
}
