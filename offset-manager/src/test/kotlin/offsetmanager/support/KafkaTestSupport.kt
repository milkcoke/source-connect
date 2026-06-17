package offsetmanager.support

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.TopicDescription
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.internals.AutoOffsetResetStrategy
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.IsolationLevel
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.record.internal.CompressionType
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions
import org.awaitility.Awaitility
import org.awaitility.core.ThrowingRunnable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.config.TopicBuilder
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class KafkaTestSupport(
  private val log: Logger = LoggerFactory.getLogger(KafkaTestSupport::class.java),
) {
  /**
   * Creates a topic with specific partitions and configs.
   * Use this in a @BeforeAll or at the start of your test.
   */
  protected fun createTestTopic(topicName: String, partitions: Int) {
    try {
      val newTopic = TopicBuilder
        .name(topicName)
        .partitions(partitions) // Replicas should be set to 1 because the kafka broker is single node in the test container
        .replicas(1)
        .compact()
        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1")
        .config(TopicConfig.SEGMENT_MS_CONFIG, "10000")
        .build()

      adminClient.createTopics(mutableListOf<NewTopic?>(newTopic)).all().get()
      Awaitility.await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(500))
        .ignoreExceptions()
        .untilAsserted(ThrowingRunnable {
          val description: MutableMap<String?, TopicDescription?> =
            adminClient.describeTopics(mutableListOf<String?>(topicName))
              .allTopicNames().get()
          Assertions.assertThat(description.containsKey(topicName)).isTrue()
          Assertions.assertThat(description[topicName]!!.partitions().size).isEqualTo(partitions)
        })
    } catch (e: Exception) {
      log.error("Topic Creation Failed to: {}", e.message)
    }
  }

  // Helper method to create a producer pointing to the container
  protected fun createProducer(): KafkaProducer<String?, Long?> {
    val props = mapOf<String, Any>(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to  LongSerializer::class.java,
      ProducerConfig.TRANSACTIONAL_ID_CONFIG to "kafka-test-producer",
      ProducerConfig.ACKS_CONFIG to "all",
      ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to  true,
      ProducerConfig.COMPRESSION_TYPE_CONFIG to CompressionType.LZ4.name
    )
    return KafkaProducer(props)
  }

  companion object {
    /** static fields will be shared between test methods.
     * Started only once before any test methods are executed and stopped after the last test method has executed.
     *
     * Default kafka lister address are localhost:9092, localhost:9093, localhost:9094
     */
    @JvmStatic
    @Container
    protected val kafkaContainer: KafkaContainer = KafkaContainer(
      DockerImageName.parse("apache/kafka:4.3.0")
    )
    @JvmStatic
    protected val testConsumerProperties: Properties = Properties()

    private lateinit var adminClient: AdminClient

    @JvmStatic
    @BeforeAll
    fun init() {
      // just to initialize the container before tests
      kafkaContainer.start()
      val adminProps = mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers
      )
      adminClient = AdminClient.create(adminProps)
      testConsumerProperties.putAll(
        mapOf(
          CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.getBootstrapServers(),
          ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to  StringDeserializer::class.java,
          ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java,
          ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG to  57671680,  // 55MB
          ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 50000,
          ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to  AutoOffsetResetStrategy.EARLIEST.name(),
          ConsumerConfig.ISOLATION_LEVEL_CONFIG to IsolationLevel.READ_COMMITTED.toString()
        )
      )
    }

    @JvmStatic
    @AfterAll
    fun cleanup(): Unit {
      kafkaContainer.close()
      adminClient.close()
    }
  }
}
