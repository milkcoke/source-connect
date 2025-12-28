package offsetmanager.service;

import offsetmanager.api.v1.dto.LastOffsetRecordBatchResponse;
import offsetmanager.api.v1.dto.LastOffsetRecordResponse;
import offsetmanager.domain.InMemoryOffsetStorage;
import offsetmanager.domain.OffsetStateUpdater;
import offsetmanager.domain.OffsetStateUpdaterImpl;
import offsetmanager.domain.OffsetStorage;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.exception.OffsetNotFoundException;
import offsetmanager.manager.OffsetManager;
import offsetmanager.repository.OffsetManagerRepository;
import offsetmanager.support.KafkaTestSupport;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class OffsetManagerServiceTest {

  private final OffsetStorage offsetStorage = new InMemoryOffsetStorage();

  @Nested
  class IntegrationTest extends KafkaTestSupport {
    private KafkaProducer<String, Long> producer;
    @BeforeAll
    void setup() {
      producer = createProducer();
      this.producer.initTransactions();
    }
    @AfterAll
    void teardown() {
      producer.close();
    }

    @DisplayName("Should return last offset of FileKey")
    @Test
    void lastFileKeyOffsetTest() throws InterruptedException {
      // given
      String offsetTopic = "temp-offset-manager-test";
      createTestTopic(offsetTopic, 1);
      // FIXME: Dependency on the interface instead of concrete class
      OffsetStateUpdater offsetStateUpdater = new OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage);
      offsetStateUpdater.start();
      OffsetManagerRepository offsetManager = new OffsetManagerRepository(offsetStorage, offsetStateUpdater);
      produceFileKeyOffset(offsetTopic, FileKeyParser.parse("file:///test-file.txt"), 100L);
      Thread.sleep(500L);
      OffsetManagerService offsetManagerService = new OffsetManagerService(offsetManager);
      // when
      LastOffsetRecordResponse response = offsetManagerService.readLastOffset("file:///test-file.txt");

      // then
      assertThat(response.key()).isEqualTo("file:///test-file.txt");
      assertThat(response.offset()).isEqualTo(100L);

      // cleans
      offsetStateUpdater.stop();
    }

    @DisplayName("Should return last offset list of FileKeys")
    @Test
    void lastFileKeysOffsetTest() throws InterruptedException {
      String offsetTopic = "temp-offset-manager-test";
      createTestTopic(offsetTopic, 1);
      OffsetStateUpdater offsetStateUpdater = new OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage);
      offsetStateUpdater.start();
      OffsetManagerRepository offsetManager = new OffsetManagerRepository(offsetStorage, offsetStateUpdater);
      produceFileKeyOffset(offsetTopic, FileKeyParser.parse("s3://test-bucket/file-key-1.txt"), 100L);
      produceFileKeyOffset(offsetTopic, FileKeyParser.parse("s3://test-bucket/file-key-2.txt"), 100L);
      produceFileKeyOffset(offsetTopic, FileKeyParser.parse("s3://test-bucket/file-key-3.txt"), 100L);

      Thread.sleep(500L);
      OffsetManagerService offsetManagerService = new OffsetManagerService(offsetManager);
      // when
      LastOffsetRecordBatchResponse response = offsetManagerService.readLastOffsets(List.of(
        "s3://test-bucket/file-key-1.txt",
        "s3://test-bucket/file-key-2.txt",
        "s3://test-bucket/file-key-3.txt"
      ));
      // then
      assertThat(response.lastOffsetRecords()).hasSize(3)
        .containsExactlyInAnyOrder(
          new LastOffsetRecordResponse("s3://test-bucket/file-key-1.txt", 100L),
          new LastOffsetRecordResponse("s3://test-bucket/file-key-2.txt", 100L),
          new LastOffsetRecordResponse("s3://test-bucket/file-key-3.txt", 100L)
        );
      // cleans
      offsetStateUpdater.stop();
    }

    @DisplayName("Should throw OffsetNotFoundException when the key does not exist")
    @Test
    void returnEmptyWhenNotExistKey() {
      // given
      String offsetTopic = "temp-offset-manager-test";
      createTestTopic(offsetTopic, 1);
      OffsetStateUpdater offsetStateUpdater = new OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage);
      offsetStateUpdater.start();
      OffsetManagerRepository offsetManager = new OffsetManagerRepository(offsetStorage, offsetStateUpdater);
      OffsetManagerService offsetManagerService = new OffsetManagerService(offsetManager);
      // when then
      assertThatThrownBy(() -> offsetManagerService.readLastOffset("file:///notExistKey.txt"))
        .isInstanceOf(OffsetNotFoundException.class)
        .hasMessage("Offset not found for key: file:///notExistKey.txt");
      // cleans
      offsetStateUpdater.stop();
    }

    @DisplayName("Should return empty batch when none of the keys exist")
    @Test
    void returnEmptyBatchWhenNotExistsKey() {
      // given
      String offsetTopic = "temp-offset-manager-test";
      createTestTopic(offsetTopic, 1);
      OffsetStateUpdater offsetStateUpdater = new OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage);
      offsetStateUpdater.start();
      OffsetManagerRepository offsetManager = new OffsetManagerRepository(offsetStorage, offsetStateUpdater);
      OffsetManagerService offsetManagerService = new OffsetManagerService(offsetManager);

      // when
      LastOffsetRecordBatchResponse response = offsetManagerService.readLastOffsets(List.of(
        "s3://test-bucket/non-exist-key-1.txt",
        "s3://test-bucket/non-exist-key-2.txt"
      ));
      // then
      assertThat(response.lastOffsetRecords()).isEmpty();
      // cleans
      offsetStateUpdater.stop();
    }

    void produceFileKeyOffset(String offsetTopic, FileKey fileKey, long offset) {
      producer.beginTransaction();
      String fileKeyStr = fileKey.get();
      producer.send(new ProducerRecord<>(offsetTopic, fileKeyStr, offset));
      producer.commitTransaction();
    }
  }

  @Nested
  class UnitTest {
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
}
