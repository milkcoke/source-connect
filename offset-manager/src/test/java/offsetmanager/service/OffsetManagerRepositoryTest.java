package offsetmanager.service;

import offsetmanager.KafkaTestSupport;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetRecord;
import offsetmanager.exception.OffsetNotFoundException;
import offsetmanager.manager.OffsetManager;
import offsetmanager.repository.OffsetManagerRepository;
import offsetmanager.service.dto.LastOffsetRecordResponse;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class OffsetManagerRepositoryTest extends KafkaTestSupport {

  @DisplayName("Should return last offset of FileKey")
  @Test
  void lastFileKeyOffsetTest() throws InterruptedException {
    // given
    String offsetTopic = "temp-offset-manager-test";
    createTestTopic(offsetTopic, 1);
    OffsetManager offsetManager = new OffsetManagerRepository(createConsumer(), offsetTopic);
    try(KafkaProducer<String, Long> producer = createProducer()){
      producer.initTransactions();
      producer.beginTransaction();
      for (int i = 0; i <= 100; i++) {
        FileKey fileKey = FileKeyParser.parse("file:///test-file.txt");
        OffsetRecord offsetRecord = new DefaultOffsetRecord(fileKey, i);
        producer.send(new ProducerRecord<>(
          offsetTopic,
          offsetRecord.key().get(),
          offsetRecord.offset())
        );
      }
      producer.commitTransaction();

      Thread.sleep(500L);
      // when
      Optional<OffsetRecord> latestOffsetRecord = offsetManager.findLatestOffsetRecord(
        FileKeyParser.parse("file:///test-file.txt")
      );

      // then
      assertThat(latestOffsetRecord.get()).isEqualTo(
        new DefaultOffsetRecord(
          FileKeyParser.parse("file:///test-file.txt"),
          100L
        )
      );
    }
  }

  @DisplayName("Should return offset when the key exists")
  @Test
  void readLastOffset() {
    // given
    OffsetManager mockManager = Mockito.mock(OffsetManager.class);
    FileKey fileKey = FileKeyParser.parse("file:///existKey.txt");

    when(mockManager.findLatestOffsetRecord(fileKey))
      .thenReturn(Optional.of(new DefaultOffsetRecord(fileKey, 10L)));

    OffsetManagerService remoteOffsetService = new OffsetManagerService(mockManager);
    // when
    LastOffsetRecordResponse lastOffsetRecordResponse = remoteOffsetService.readLastOffset("file:///existKey.txt");
    // then
    assertThat(lastOffsetRecordResponse.key()).isEqualTo("file:///existKey.txt");
    assertThat(lastOffsetRecordResponse.offset()).isEqualTo(10L);
  }

  @DisplayName("Should throw OffsetNotFoundException when the key does not exist")
  @Test
  void returnEmptyWhenNotExistKey() {
    // given
    OffsetManager mockManager = Mockito.mock(OffsetManager.class);
    FileKey nonExistFileKey = FileKeyParser.parse("file:///nonExistKey.txt");
    when(mockManager.findLatestOffsetRecord(nonExistFileKey)).thenReturn(Optional.empty());

    OffsetManagerService remoteOffsetService = new OffsetManagerService(mockManager);
    // when then
    assertThatThrownBy(()-> remoteOffsetService.readLastOffset("file:///notExistKey.txt"))
      .isInstanceOf(OffsetNotFoundException.class)
      .hasMessage("Offset not found for key: file:///notExistKey.txt");

  }
}
