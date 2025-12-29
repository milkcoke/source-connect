package offsetmanager.domain

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.util.*

internal class InMemoryOffsetStorageTest {
  @DisplayName("Should return Optional empty when key not found")
  @Test
  fun findEmptyTest() {
    // given
    val offsetStorage: OffsetStorage = InMemoryOffsetStorage()
    val notExistFileKey: FileKey = from(Path.of("non-existing-file.txt"))
    // when
    val offsetRecord: Optional<OffsetRecord> = offsetStorage.find(notExistFileKey)
    // then
    assertThat<OffsetRecord?>(offsetRecord).isEmpty()
  }

  @DisplayName("Should return FileKey when key found")
  @Test
  fun findTest() {
    // given
    val offsetStorage: OffsetStorage = InMemoryOffsetStorage()
    val existFileKey: FileKey = from(Path.of("existing-file.txt"))
    offsetStorage.upsert(existFileKey, DefaultOffsetRecord(existFileKey, 0))
    // when
    val offsetRecord: Optional<OffsetRecord> = offsetStorage.find(existFileKey)
    // then
    assertThat<OffsetRecord?>(offsetRecord.get()).isEqualTo(DefaultOffsetRecord(existFileKey, 0))
  }

  @DisplayName("Should overwrite existing record on upsert")
  @Test
  fun upsert() {
    // given
    val offsetStorage: OffsetStorage = InMemoryOffsetStorage()
    val existFileKey: FileKey = from(Path.of("existing-file.txt"))
    // when
    offsetStorage.upsert(existFileKey, DefaultOffsetRecord(existFileKey, 0))
    offsetStorage.upsert(existFileKey, DefaultOffsetRecord(existFileKey, 1))
    val offsetRecord: Optional<OffsetRecord> = offsetStorage.find(existFileKey)
    // then
    assertThat<OffsetRecord?>(offsetRecord.get()).isEqualTo(DefaultOffsetRecord(existFileKey, 1))
  }

  @DisplayName("Should remove record on remove")
  @Test
  fun remove() {
    // given
    val offsetStorage: OffsetStorage = InMemoryOffsetStorage()
    val existFileKey: FileKey = from(Path.of("existing-file.txt"))
    offsetStorage.upsert(existFileKey, DefaultOffsetRecord(existFileKey, 0))
    // when
    offsetStorage.remove(existFileKey)
    val offsetRecord: Optional<OffsetRecord> = offsetStorage.find(existFileKey)
    // then
    assertThat(offsetRecord).isEmpty()
  }

  @DisplayName("Should clear all records on clear")
  @Test
  fun clear() {
    // given
    val offsetStorage: OffsetStorage = InMemoryOffsetStorage()
    val existFileKey1: FileKey = from(Path.of("existing-file1.txt"))
    val existFileKey2: FileKey = from(Path.of("existing-file2.txt"))
    offsetStorage.upsert(existFileKey1, DefaultOffsetRecord(existFileKey1, 0))
    offsetStorage.upsert(existFileKey2, DefaultOffsetRecord(existFileKey2, 1))
    // when
    offsetStorage.clear()
    val offsetRecord1: Optional<OffsetRecord> = offsetStorage.find(existFileKey1)
    val offsetRecord2: Optional<OffsetRecord> = offsetStorage.find(existFileKey1)
    // then
    assertThat(offsetRecord1).isEmpty()
    assertThat(offsetRecord2).isEmpty()
  }
}
