package offsetmanager.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.OffsetStateReadiness;
import offsetmanager.domain.OffsetStorage;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;
import offsetmanager.manager.OffsetManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Should update continuously when new offsets are produced to the offset topic <br>
 * without consumer group management in the background
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OffsetManagerRepository implements OffsetManager {
  private final OffsetStorage offsetStorage;
  private final OffsetStateReadiness offsetStateReadiness;

  @Override
  public Optional<OffsetRecord> findLatestOffsetRecord(FileKey key) {
    this.offsetStateReadiness.awaitReady();
    return this.offsetStorage.find(key);
  }

  @Override
  public List<OffsetRecord> findLatestOffsetRecords(List<FileKey> keys) {
    this.offsetStateReadiness.awaitReady();
    return keys.stream()
      .map(offsetStorage::find)
      .flatMap(Optional::stream)
      .toList();
  }
}
