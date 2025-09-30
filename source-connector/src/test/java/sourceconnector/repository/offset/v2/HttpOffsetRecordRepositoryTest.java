package sourceconnector.repository.offset.v2;

import offsetmanager.domain.OffsetRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class HttpOffsetRecordRepositoryTest {
    @DisplayName("Should get last offset record when exists")
    @Test
    void findLastOffsetRecordTest() {
      // given
      OffsetRecordRepository repository = new HttpOffsetRecordRepository("http://localhost:8080");
      // when
      Optional<OffsetRecord> offsetRecord = repository.findLastOffsetRecord("test-key1");

        // then
      assertThat(offsetRecord)
              .isPresent();
    }

    @DisplayName("Should get empty when last offset record not exists")
    @Test
    void failedToGetRecordTest() {
      // given
      OffsetRecordRepository repository = new HttpOffsetRecordRepository("http://localhost:8080");
      // when
      Optional<OffsetRecord> offsetRecord = repository.findLastOffsetRecord("not-exists-key1");
      // then
      assertThat(offsetRecord).isEmpty();
    }

    @DisplayName("Should get last offset record list when exists")
    @Test
    void findLastOffsetRecordsTest() {
        // given
        OffsetRecordRepository repository = new HttpOffsetRecordRepository("http://localhost:8080");
        // when
        List<OffsetRecord> offsetRecords = repository.findLastOffsetRecords(List.of(
            "test-key2",
            "test-key3",
            "test-key4"
        ));

        // then
        assertThat(offsetRecords)
                .isNotEmpty();

    }

    @DisplayName("Should get empty list when last offset record not exists")
    @Test
    void failedToGetRecordListTest() {
        // given
        OffsetRecordRepository repository = new HttpOffsetRecordRepository("http://localhost:8080");
        // when
        List<OffsetRecord> offsetRecord = repository.findLastOffsetRecords(List.of(
            "not-exists-key2",
            "not-exists-key3",
            "not-exists-key4"
        ));
        // then
        assertThat(offsetRecord).isEmpty();
    }

}
