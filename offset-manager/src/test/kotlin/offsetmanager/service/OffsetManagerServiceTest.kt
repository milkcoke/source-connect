package offsetmanager.service

import offsetmanager.api.v1.dto.LastOffsetRecordResponse
import offsetmanager.domain.InMemoryOffsetStorage
import offsetmanager.domain.OffsetStateUpdater
import offsetmanager.domain.OffsetStateUpdaterImpl
import offsetmanager.domain.OffsetStorage
import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.factory.FileKeyParser.Companion.parse
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetRecord
import offsetmanager.exception.OffsetNotFoundException
import offsetmanager.manager.OffsetManager
import offsetmanager.repository.OffsetManagerRepository
import offsetmanager.support.KafkaTestSupport
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.*
import org.mockito.Mockito
import java.util.*

internal class OffsetManagerServiceTest {
  private val offsetStorage: OffsetStorage = InMemoryOffsetStorage()

  @Nested
  internal inner class IntegrationTest : KafkaTestSupport() {
    private lateinit var producer: KafkaProducer<String?, Long?>

    @BeforeAll
    fun setup() {
      producer = createProducer()
      this.producer.initTransactions()
    }

    @AfterAll
    fun teardown() {
      producer.close()
    }

    @DisplayName("Should return last offset of FileKey")
    @Test
    @Throws(InterruptedException::class)
    fun lastFileKeyOffsetTest() {
      // given
      val offsetTopic = "temp-offset-manager-test"
      createTestTopic(offsetTopic, 1)
      // FIXME: Dependency on the interface instead of concrete class
      val offsetStateUpdater: OffsetStateUpdater =
        OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage)
      offsetStateUpdater.start()
      val offsetManager = OffsetManagerRepository(offsetStorage, offsetStateUpdater)
      produceFileKeyOffset(offsetTopic, parse("file:///test-file.txt"), 100L)
      Thread.sleep(500L)
      val offsetManagerService = OffsetManagerService(offsetManager)
      // when
      val response = offsetManagerService.readLastOffset("file:///test-file.txt")

      // then
      Assertions.assertThat(response.key).isEqualTo("file:///test-file.txt")
      Assertions.assertThat(response.offset).isEqualTo(100L)

      // cleans
      offsetStateUpdater.stop()
    }

    @DisplayName("Should return last offset list of FileKeys")
    @Test
    @Throws(InterruptedException::class)
    fun lastFileKeysOffsetTest() {
      val offsetTopic = "temp-offset-manager-test"
      createTestTopic(offsetTopic, 1)
      val offsetStateUpdater: OffsetStateUpdater =
        OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage)
      offsetStateUpdater.start()
      val offsetManager = OffsetManagerRepository(offsetStorage, offsetStateUpdater)
      produceFileKeyOffset(offsetTopic, parse("s3://test-bucket/file-key-1.txt"), 100L)
      produceFileKeyOffset(offsetTopic, parse("s3://test-bucket/file-key-2.txt"), 100L)
      produceFileKeyOffset(offsetTopic, parse("s3://test-bucket/file-key-3.txt"), 100L)

      Thread.sleep(500L)
      val offsetManagerService = OffsetManagerService(offsetManager)
      // when
      val response = offsetManagerService.readLastOffsets(
        listOf(
          "s3://test-bucket/file-key-1.txt",
          "s3://test-bucket/file-key-2.txt",
          "s3://test-bucket/file-key-3.txt"
        )
      )
      // then
      Assertions.assertThat<LastOffsetRecordResponse>(response.lastOffsetRecords).hasSize(3)
        .containsExactlyInAnyOrder(
          LastOffsetRecordResponse("s3://test-bucket/file-key-1.txt", 100L),
          LastOffsetRecordResponse("s3://test-bucket/file-key-2.txt", 100L),
          LastOffsetRecordResponse("s3://test-bucket/file-key-3.txt", 100L)
        )
      // cleans
      offsetStateUpdater.stop()
    }

    @DisplayName("Should throw OffsetNotFoundException when the key does not exist")
    @Test
    fun returnEmptyWhenNotExistKey() {
      // given
      val offsetTopic = "temp-offset-manager-test"
      createTestTopic(offsetTopic, 1)
      val offsetStateUpdater: OffsetStateUpdater =
        OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage)
      offsetStateUpdater.start()
      val offsetManager = OffsetManagerRepository(offsetStorage, offsetStateUpdater)
      val offsetManagerService = OffsetManagerService(offsetManager)
      // when then
      Assertions.assertThatThrownBy( { offsetManagerService.readLastOffset("file:///notExistKey.txt") })
        .isInstanceOf(OffsetNotFoundException::class.java)
        .hasMessage("Offset not found for key: file:///notExistKey.txt")
      // cleans
      offsetStateUpdater.stop()
    }

    @DisplayName("Should return empty batch when none of the keys exist")
    @Test
    fun returnEmptyBatchWhenNotExistsKey() {
      // given
      val offsetTopic = "temp-offset-manager-test"
      createTestTopic(offsetTopic, 1)
      val offsetStateUpdater: OffsetStateUpdater =
        OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage)
      offsetStateUpdater.start()
      val offsetManager = OffsetManagerRepository(offsetStorage, offsetStateUpdater)
      val offsetManagerService = OffsetManagerService(offsetManager)

      // when
      val response = offsetManagerService.readLastOffsets(
        listOf(
          "s3://test-bucket/non-exist-key-1.txt",
          "s3://test-bucket/non-exist-key-2.txt"
        )
      )
      // then
      Assertions.assertThat<LastOffsetRecordResponse>(response.lastOffsetRecords).isEmpty()
      // cleans
      offsetStateUpdater.stop()
    }

    fun produceFileKeyOffset(offsetTopic: String, fileKey: FileKey, offset: Long) {
      producer.beginTransaction()
      val fileKeyStr = fileKey.get()
      producer.send(ProducerRecord(offsetTopic, fileKeyStr, offset))
      producer.commitTransaction()
    }
  }

  @Nested
  internal inner class UnitTest {
    @DisplayName("Should return offset when the key exists")
    @Test
    fun readLastOffset() {
      // given
      val mockManager = Mockito.mock<OffsetManager>(OffsetManager::class.java)
      val fileKey = parse("file:///existKey.txt")

      Mockito.`when`<Optional<OffsetRecord>>(mockManager.findLatestOffsetRecord(fileKey))
        .thenReturn(Optional.of(DefaultOffsetRecord(fileKey, 10L)))

      val remoteOffsetService = OffsetManagerService(mockManager)
      // when
      val lastOffsetRecordResponse = remoteOffsetService.readLastOffset("file:///existKey.txt")
      // then
      Assertions.assertThat(lastOffsetRecordResponse.key).isEqualTo("file:///existKey.txt")
      Assertions.assertThat(lastOffsetRecordResponse.offset).isEqualTo(10L)
    }

    @DisplayName("Should throw OffsetNotFoundException when the key does not exist")
    @Test
    fun returnEmptyWhenNotExistKey() {
      // given
      val mockManager = Mockito.mock<OffsetManager>(OffsetManager::class.java)
      val nonExistFileKey = parse("file:///nonExistKey.txt")
      Mockito.`when`<Optional<OffsetRecord>>(mockManager.findLatestOffsetRecord(nonExistFileKey)).thenReturn(
        Optional.empty()
      )

      val remoteOffsetService = OffsetManagerService(mockManager)
      // when then
      Assertions.assertThatThrownBy { remoteOffsetService.readLastOffset("file:///notExistKey.txt") }
        .isInstanceOf(OffsetNotFoundException::class.java)
        .hasMessage("Offset not found for key: file:///notExistKey.txt")
    }
  }
}
