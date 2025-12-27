package offsetmanager.domain;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOffsetStorage implements OffsetStorage {
  private final Map<FileKey, OffsetRecord> offsetMap = new ConcurrentHashMap<>();

  @Override
  public Optional<OffsetRecord> find(FileKey key) {
    if (this.offsetMap.containsKey(key)) {
      return Optional.of(this.offsetMap.get(key));
    }
    return Optional.empty();
  }

  @Override
  public void upsert(FileKey key, OffsetRecord offsetRecord) {
    this.offsetMap.put(key, offsetRecord);
  }

  @Override
  public void remove(FileKey fileKey) {
    this.offsetMap.remove(fileKey);
  }

  @Override
  public void clear() {
    this.offsetMap.clear();
  }
}
