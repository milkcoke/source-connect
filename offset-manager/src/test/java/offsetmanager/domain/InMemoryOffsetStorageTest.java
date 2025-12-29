package offsetmanager.domain;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryOffsetStorageTest {

  @DisplayName("Should return Optional empty when key not found")
  @Test
  void findEmptyTest() {
    // given
    OffsetStorage offsetStorage = new InMemoryOffsetStorage();
    FileKey notExistFileKey = LocalFileKey.from(Path.of("non-existing-file.txt"));
    // when
    Optional<OffsetRecord> offsetRecord = offsetStorage.find(notExistFileKey);
    // then
    assertThat(offsetRecord).isEmpty();
  }

  @DisplayName("Should return FileKey when key found")
  @Test
  void findTest() {
    // given
    OffsetStorage offsetStorage = new InMemoryOffsetStorage();
    FileKey existFileKey = LocalFileKey.from(Path.of("existing-file.txt"));
    offsetStorage.upsert(existFileKey, new DefaultOffsetRecord(existFileKey, 0));
    // when
    Optional<OffsetRecord> offsetRecord = offsetStorage.find(existFileKey);
    // then
    assertThat(offsetRecord.get()).isEqualTo(new DefaultOffsetRecord(existFileKey, 0));
  }

  @DisplayName("Should overwrite existing record on upsert")
  @Test
  void upsert() {
    // given
    OffsetStorage offsetStorage = new InMemoryOffsetStorage();
    FileKey existFileKey = LocalFileKey.from(Path.of("existing-file.txt"));
    // when
    offsetStorage.upsert(existFileKey, new DefaultOffsetRecord(existFileKey, 0));
    offsetStorage.upsert(existFileKey, new DefaultOffsetRecord(existFileKey, 1));
    Optional<OffsetRecord> offsetRecord = offsetStorage.find(existFileKey);
    // then
    assertThat(offsetRecord.get()).isEqualTo(new DefaultOffsetRecord(existFileKey, 1));
  }

  @DisplayName("Should remove record on remove")
  @Test
  void remove() {
    // given
    OffsetStorage offsetStorage = new InMemoryOffsetStorage();
    FileKey existFileKey = LocalFileKey.from(Path.of("existing-file.txt"));
    offsetStorage.upsert(existFileKey, new DefaultOffsetRecord(existFileKey, 0));
    // when
    offsetStorage.remove(existFileKey);
    Optional<OffsetRecord> offsetRecord = offsetStorage.find(existFileKey);
    // then
    assertThat(offsetRecord).isEmpty();
  }

  @DisplayName("Should clear all records on clear")
  @Test
  void clear() {
    // given
    OffsetStorage offsetStorage = new InMemoryOffsetStorage();
    FileKey existFileKey1 = LocalFileKey.from(Path.of("existing-file1.txt"));
    FileKey existFileKey2 = LocalFileKey.from(Path.of("existing-file2.txt"));
    offsetStorage.upsert(existFileKey1, new DefaultOffsetRecord(existFileKey1, 0));
    offsetStorage.upsert(existFileKey2, new DefaultOffsetRecord(existFileKey2, 1));
    // when
    offsetStorage.clear();
    Optional<OffsetRecord> offsetRecord1 = offsetStorage.find(existFileKey1);
    Optional<OffsetRecord> offsetRecord2 = offsetStorage.find(existFileKey1);
    // then
    assertThat(offsetRecord1).isEmpty();
    assertThat(offsetRecord2).isEmpty();
  }
}
