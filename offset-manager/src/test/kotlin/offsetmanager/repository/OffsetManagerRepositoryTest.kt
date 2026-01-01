package offsetmanager.repository

import offsetmanager.domain.InMemoryOffsetStorage
import offsetmanager.domain.OffsetStateUpdater
import offsetmanager.domain.OffsetStateUpdaterImpl
import offsetmanager.domain.OffsetStorage
import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.factory.FileKeyParser.Companion.parse
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetRecord
import offsetmanager.exception.OffsetManagerNotReadyException
import offsetmanager.support.KafkaTestSupport
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*
import java.util.List

internal class OffsetManagerRepositoryTest: KafkaTestSupport() {
  private val offsetStorage: OffsetStorage = InMemoryOffsetStorage()
  private val offsetTopic: String = "remote-offset-topic"
  private lateinit var producer: Producer<String?, Long?>

  @BeforeAll
  fun setup() {
    this.producer = createProducer()
    producer.initTransactions()
    createTestTopic(offsetTopic, 3)
  }

  @AfterAll
  fun teardown() {
    producer.close()
  }

  @DisplayName("Nothing to do update but background update thread starts")
  @Test
  fun emptyOffsetTopicTest() {
    // given
    val offsetStateUpdater: OffsetStateUpdater =
      OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage)
    offsetStateUpdater.start()
    val offsetManager = OffsetManagerRepository(offsetStorage, offsetStateUpdater)
    // when
    val foundOffset: OffsetRecord? = offsetManager.findLatestOffsetRecord(parse("file:///test/path.txt"))
    // then
    assertThat<OffsetRecord>(foundOffset).isNull()
  }

  @DisplayName("Should throw OffsetManagerNotReadyException when OffsetStateUpdater not started")
  @Test
  fun offsetUpdaterNotReadyTest() {
    // given
    val offsetStateUpdater: OffsetStateUpdater =
      OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage)
    val offsetManager = OffsetManagerRepository(offsetStorage, offsetStateUpdater)
    // when then
    Assertions.assertThatThrownBy { offsetManager.findLatestOffsetRecord(parse("file:///test/path.txt")) }
      .isInstanceOf(OffsetManagerNotReadyException::class.java)
  }

  @DisplayName("Should find all offset records by keys")
  @Test
  @Throws(InterruptedException::class)
  fun findAllOffsetRecordsTest() {
    // given
    val offsetStateUpdater: OffsetStateUpdater =
      OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage)
    offsetStateUpdater.start()
    val offsetManager = OffsetManagerRepository(offsetStorage, offsetStateUpdater)
    val keyA = parse("file:///many-a.txt")
    val keyB = parse("file:///many-b.txt")
    val keyC = parse("file:///many-c.txt")
    for (i in 1..1000) {
      if (((i - 1) % 100).toLong() == 0L) {
        producer.beginTransaction()
      }
      producer.send(ProducerRecord(this.offsetTopic, keyA.get(), i.toLong()))
      producer.send(ProducerRecord(this.offsetTopic, keyB.get(), i.toLong()))
      producer.send(ProducerRecord(this.offsetTopic, keyC.get(), i.toLong()))
      if ((i % 100).toLong() == 0L) {
        producer.commitTransaction()
      }
    }
    Thread.sleep(1000)

    // when
    val offsetRecords: kotlin.collections.List<OffsetRecord> = offsetManager.findLatestOffsetRecords(listOf(keyA, keyB, keyC))
    // then
    assertThat<OffsetRecord?>(offsetRecords)
      .hasSize(3)
      .containsExactlyInAnyOrder(
        DefaultOffsetRecord(keyA, 1000L),
        DefaultOffsetRecord(keyB, 1000L),
        DefaultOffsetRecord(keyC, 1000L)
      )

    // cleans
    offsetStateUpdater.stop()
  }

  @DisplayName("Update continuously receives new offsets and updates the store")
  @Test
  @Throws(InterruptedException::class)
  fun upsertContinuously() {
    // given
    val offsetStateUpdater: OffsetStateUpdater =
      OffsetStateUpdaterImpl(offsetTopic, testConsumerProperties, offsetStorage)
    offsetStateUpdater.start()
    val offsetManager = OffsetManagerRepository(offsetStorage, offsetStateUpdater)
    val keyA = parse("file:///key-a.txt")
    val keyB = parse("file:///key-b.txt")
    val keyC = parse("file:///key-c.txt")
    assertThat<OffsetRecord>(offsetManager.findLatestOffsetRecord(keyA)).isNull()
    assertThat<OffsetRecord>(offsetManager.findLatestOffsetRecord(keyB)).isNull()
    assertThat<OffsetRecord>(offsetManager.findLatestOffsetRecord(keyC)).isNull()

    for (i in 1..1000) {
      if (((i - 1) % 100).toLong() == 0L) {
        producer.beginTransaction()
      }
      producer.send(ProducerRecord(this.offsetTopic, keyA.get(), i.toLong()))
      producer.send(ProducerRecord(this.offsetTopic, keyB.get(), i.toLong()))
      producer.send(ProducerRecord(this.offsetTopic, keyC.get(), i.toLong()))
      if ((i % 100).toLong() == 0L) {
        producer.commitTransaction()
      }
    }
    // when then
    Thread.sleep(1000)
    assertThat<OffsetRecord>(offsetManager.findLatestOffsetRecord(keyA))
      .isEqualTo(DefaultOffsetRecord(keyA, 1000L))
    assertThat<OffsetRecord>(offsetManager.findLatestOffsetRecord(keyB))
      .isEqualTo(DefaultOffsetRecord(keyB, 1000L))
    assertThat<OffsetRecord>(offsetManager.findLatestOffsetRecord(keyC))
      .isEqualTo(DefaultOffsetRecord(keyC, 1000L))

    // cleans
    offsetStateUpdater.stop()
  }
}
