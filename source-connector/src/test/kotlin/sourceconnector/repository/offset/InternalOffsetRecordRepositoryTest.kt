package sourceconnector.repository.offset

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import offsetmanager.domain.file.factory.FileKeyParser.Companion.parse
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.support.KafkaTestSupport
import java.nio.ByteBuffer
import java.nio.file.Path
import java.util.*
import java.util.function.Consumer
import kotlin.ByteArray
import kotlin.Exception
import kotlin.String
import kotlin.Throws
import kotlin.use

internal class InternalOffsetRecordRepositoryTest : KafkaTestSupport() {
  private val offsetTopicName = "internal-offset-test"
  private lateinit var producer: KafkaProducer<String?, ByteArray>

  @BeforeAll
  fun setup() {
    createOffsetTopic(offsetTopicName, 2)
    this.producer = createProducer()
    this.producer.initTransactions()
  }

  @AfterAll
  fun teardown() {
    producer.close()
  }


  @DisplayName("Should empty when no offset record exists for the given FileKey")
  @Test
  @Throws(Exception::class)
  fun notFoundOffsetTest() {
    // given
    val notExistFileKey: FileKey = from(Path.of("NotExistFile.ndjson"))
    val consumer: KafkaConsumer<String, Long> = createConsumer()
    InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
    ).use { repository ->
      // when
      val offsetRecord: OffsetRecord? = repository.findLastOffsetRecord(notExistFileKey)
      // then
      assertThat<OffsetRecord?>(offsetRecord).isNull()
    }
  }

  @DisplayName("Get last offset record for a FileKey")
  @Test
  @Throws(Exception::class)
  fun findLastOffsetRecord() {
    // given
    val fileKey = parse("file:///sample-data.ndjson")

    producer.beginTransaction()
    for (offset in 0..100) {
      val record: OffsetRecord = DefaultOffsetRecord(fileKey, offset.toLong())

      producer.send(
        ProducerRecord<String?, ByteArray?>(
          this.offsetTopicName,
          record.key().get(),
          ByteBuffer.allocate(java.lang.Long.BYTES)
            .putLong(record.offset())
            .array()
        )
      )
    }
    producer.commitTransaction()
    val consumer: KafkaConsumer<String, Long> = createConsumer()
    val adminClient = createAdminClient()

    InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
    ).use { repository ->
      // when
      val offsetRecord: OffsetRecord = repository.findLastOffsetRecord(fileKey)!!
      // then
      assertThat(offsetRecord).isEqualTo(DefaultOffsetRecord(fileKey, 100L))
    }
  }

  @DisplayName("Get last offset record regardless the offset numbering for a FileKey")
  @Test
  @Throws(Exception::class)
  fun findLastOffsetReverseOffsetValueTest() {
    // given
    val fileKey = parse("file:///reverse-data.ndjson")

    producer.beginTransaction()
    for (offset in 100 downTo 0) {
      val record: OffsetRecord = DefaultOffsetRecord(fileKey, offset.toLong())

      producer.send(
        ProducerRecord<String?, ByteArray?>(
          this.offsetTopicName,
          record.key().get(),
          ByteBuffer.allocate(java.lang.Long.BYTES)
            .putLong(record.offset())
            .array()
        )
      )
    }
    producer.commitTransaction()

    val consumer: KafkaConsumer<String, Long> = createConsumer()
    val adminClient = createAdminClient()

    InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
    ).use { repository ->
      // when
      val offsetRecord: OffsetRecord = repository.findLastOffsetRecord(fileKey)!!

      // then
      assertThat(offsetRecord).isEqualTo(DefaultOffsetRecord(fileKey, 0L))
    }
  }

  @DisplayName("Should return empty list when no offset records exist for the given FileKeys")
  @Test
  @Throws(Exception::class)
  fun notFoundOffsetsTest() {
    // given
    val notExistFileKeys: List<FileKey> = listOf(
      from(Path.of("NotExistFile1.ndjson")),
      from(Path.of("NotExistFile2.ndjson")),
      from(Path.of("NotExistFile3.ndjson"))
    )
    val consumer: KafkaConsumer<String, Long> = createConsumer()
    val adminClient = createAdminClient()

    InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
    ).use { repository ->
      // when
      val offsetRecords: List<OffsetRecord> = repository.findLastOffsetRecords(notExistFileKeys)
      // then
      assertThat(offsetRecords).isEmpty()
    }
  }

  @DisplayName("Get last offset records for multiple FileKeys")
  @Test
  @Throws(Exception::class)
  fun findLastOffsetRecords() {
    // given
    val fileKeys: List<FileKey> = listOf(
      parse("file:///sample-data1.ndjson"),
      parse("file:///sample-data2.ndjson"),
      parse("file:///sample-data3.ndjson")
    )

    producer.beginTransaction()
    for (fileKey in fileKeys) {
      for (offset in 0..100) {
        val record: OffsetRecord = DefaultOffsetRecord(fileKey, offset.toLong())

        producer.send(
          ProducerRecord<String?, ByteArray?>(
            this.offsetTopicName,
            record.key().get(),
          ByteBuffer.allocate(java.lang.Long.BYTES)
            .putLong(record.offset())
            .array()
          )
        )
      }
    }
    producer.commitTransaction()

    val consumer: KafkaConsumer<String, Long> = createConsumer()
    val adminClient = createAdminClient()

    InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
    ).use { repository ->
      // when
      val offsetRecords: List<OffsetRecord> = repository.findLastOffsetRecords(fileKeys)
      // then
      assertThat<OffsetRecord?>(offsetRecords).hasSize(fileKeys.size)
        .containsExactlyInAnyOrder(
          DefaultOffsetRecord(fileKeys[0], 100L),
          DefaultOffsetRecord(fileKeys[1], 100L),
          DefaultOffsetRecord(fileKeys[2], 100L)
        )
    }
  }

  @DisplayName("Get last offset records in reverse")
  @Test
  @Throws(Exception::class)
  fun findLastOffsetsReverseOffsetValueTest() {
    // given
    val fileKeys: List<FileKey> = listOf(
      parse("file:///sample-data1.ndjson"),
      parse("file:///sample-data2.ndjson"),
      parse("file:///sample-data3.ndjson")
    )

    producer.beginTransaction()
    for (fileKey in fileKeys) {
      for (offset in 100 downTo -1) {
        val record: OffsetRecord = DefaultOffsetRecord(fileKey, offset.toLong())

        producer.send(
          ProducerRecord<String?, ByteArray?>(
            this.offsetTopicName,
            record.key().get(),
            ByteBuffer.allocate(java.lang.Long.BYTES)
              .putLong(record.offset())
              .array()
          )
        )
      }
    }
    producer.commitTransaction()

    val consumer: KafkaConsumer<String, Long> = createConsumer()
    val adminClient = createAdminClient()

    InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
    ).use { repository ->
      // when
      val offsetRecords: List<OffsetRecord> = repository.findLastOffsetRecords(fileKeys)
      // then
      assertThat(offsetRecords).hasSize(fileKeys.size)
        .containsExactlyInAnyOrder(
          DefaultOffsetRecord(fileKeys[0], -1L),
          DefaultOffsetRecord(fileKeys[1], -1L),
          DefaultOffsetRecord(fileKeys[2], -1L)
        )
    }
  }
}
