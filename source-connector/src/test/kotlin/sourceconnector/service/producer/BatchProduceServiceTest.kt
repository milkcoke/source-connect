package sourceconnector.service.producer

import offsetmanager.domain.file.factory.FileKeyParser.Companion.parse
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.record.CompressionType
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.domain.batch.DefaultMessageBatchLogs
import sourceconnector.domain.batch.MessageBatch
import java.nio.ByteBuffer
import java.time.Duration
import java.util.*
import java.util.List
import java.util.Map
import java.util.function.ToIntFunction

@Disabled
internal class BatchProduceServiceTest {
  @Test
  fun sendTest() {
    // given
    val batchProduceService = BatchProduceService(
      props,
      "log-topic",
      "s3-offset-topic"
    )
    val messageBatch: MessageBatch<String> = DefaultMessageBatchLogs(
      mutableListOf("log1", "log2", "log3")
    )
    val offsetRecord: OffsetRecord = DefaultOffsetRecord(
      parse("s3://test/2025/04/11/test.json"),
      3L
    )
    // when
    batchProduceService.sendBatch(offsetRecord, messageBatch)
  }

  @DisplayName("Message 1000개 미리 쌓아놓고 100개 사라지면?")
  @Test
  fun aThousandMessagesTest() {
    // given
    try {
      KafkaProducer<String?, ByteArray?>(props).use { producer ->
        for (i in 1001..1001) {
          producer.send(
            ProducerRecord(
              "offset-test-topic", i.toString(), ByteBuffer
                .allocate(Integer.BYTES)
                .putInt(i)
                .array()
            )
          )
        }
        producer.flush()
      }
    } catch (e: Exception) {
    }
  }

  @DisplayName("Consumer 미리 300개 쌓아놓고 첫 오프셋은 301인데 1000번까지 쌓인 상황에서 0부터 poll()")
  @Test
  fun pollThousandTest() {
    // given
    val props = Properties()
    props.putAll(
      mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ByteArrayDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to IntegerDeserializer::class.java,
        ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG to 2000,
        ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG to 52428800,
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 500
      )
    )
    KafkaConsumer<ByteArray?, Int?>(props).use { consumer ->
      val topicPartition = TopicPartition("offset-test-topic", 0)
      consumer.assign(listOf(topicPartition))
      // when
      consumer.seek(topicPartition, 100)

      // then
      val consumerRecords = consumer.poll(Duration.ofSeconds(60L))
      val records = consumerRecords.records(topicPartition)
      println("Size : " + records.size)
      println(
        "Max : " + records.stream()
          .max(Comparator.comparingInt<ConsumerRecord<ByteArray?, Int?>?>(ToIntFunction { obj: ConsumerRecord<ByteArray?, Int?>? -> obj!!.value()!! }))
      )
      println(
        "Min : " + records.stream()
          .min(Comparator.comparingInt<ConsumerRecord<ByteArray?, Int?>?>(ToIntFunction { obj: ConsumerRecord<ByteArray?, Int?>? -> obj!!.value()!! }))
      )

      val secondConsumerRecords = consumer.poll(Duration.ofSeconds(120L))
      val secondRecords = secondConsumerRecords.records(topicPartition)
      println("Size : " + secondRecords.size)
      println(
        "Max : " + secondRecords.stream().max(
          Comparator.comparingInt<ConsumerRecord<ByteArray?, Int?>?>(
            ToIntFunction { obj: ConsumerRecord<ByteArray?, Int?>? -> obj!!.value()!! })
        )
      )
      println(
        "Min : " + secondRecords.stream().min(
          Comparator.comparingInt<ConsumerRecord<ByteArray?, Int?>?>(
            ToIntFunction { obj: ConsumerRecord<ByteArray?, Int?>? -> obj!!.value()!! })
        )
      )
    }
  }

  companion object {
    private val props = Properties()

    init {
      props.putAll(
        mapOf(
          CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
          ProducerConfig.ACKS_CONFIG to "-1",
          ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
          ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
          ProducerConfig.LINGER_MS_CONFIG to 100,
          ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
          ProducerConfig.COMPRESSION_TYPE_CONFIG to CompressionType.LZ4.name
          //        ProducerConfig.TRANSACTIONAL_ID_CONFIG to "test-s3"
        )
      )
    }
  }
}
