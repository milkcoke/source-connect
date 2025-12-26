package offsetmanager.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.internals.AutoOffsetResetStrategy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.kafka.config.TopicBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ISOLATION_LEVEL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
@Slf4j
public abstract class KafkaTestSupport {

  /** static fields will be shared between test methods.
   * Started only once before any test methods are executed and stopped after the last test method has executed.
   *
   * Default kafka lister address are localhost:9092, localhost:9093, localhost:9094
   */
  @Container
  protected static final KafkaContainer kafkaContainer = new KafkaContainer(
    DockerImageName.parse("apache/kafka:4.1.1")
  );
  protected static final Properties testConsumerProperties = new Properties();

  private static AdminClient adminClient;

  @BeforeAll
  static void init() {
    // just to initialize the container before tests
    kafkaContainer.start();
    Map<String, Object> adminProps = Map.of(
      BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers()
    );
    adminClient = AdminClient.create(adminProps);
    testConsumerProperties.putAll(Map.of(
      BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
      KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class,
      MAX_PARTITION_FETCH_BYTES_CONFIG, 57_671_680, // 55MB
      MAX_POLL_RECORDS_CONFIG, 50_000,
      AUTO_OFFSET_RESET_CONFIG, AutoOffsetResetStrategy.EARLIEST.name(),
      ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString()
    ));
  }

  @AfterAll
  static void cleanup() {
    kafkaContainer.close();
    adminClient.close();
  }

  /**
   * Creates a topic with specific partitions and configs.
   * Use this in a @BeforeAll or at the start of your test.
   */
  protected void createTestTopic(String topicName, int partitions) {
    try {
      NewTopic newTopic = TopicBuilder
        .name(topicName)
        .partitions(partitions)
        // Replicas should be set to 1 because the kafka broker is single node in the test container
        .replicas(1)
        .compact()
        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1")
        .config(TopicConfig.SEGMENT_MS_CONFIG, "10000")
        .build();

      adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
      await()
        .atMost(Duration.ofSeconds(3))
        .pollInterval(Duration.ofMillis(500))
        .ignoreExceptions()
        .untilAsserted(()->{
          Map<String, TopicDescription> description = adminClient.describeTopics(Collections.singletonList(topicName))
            .allTopicNames().get();
          assertThat(description.containsKey(topicName)).isTrue();
          assertThat(description.get(topicName).partitions().size()).isEqualTo(partitions);
        });
    } catch (Exception e) {
      log.error("Topic Creation Failed to: {}", e.getMessage());
    }
  }

  // Helper method to create a producer pointing to the container
  protected KafkaProducer<String, Long> createProducer() {
    Map<String, Object> props = Map.of(
      BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
      KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
      VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class,
      TRANSACTIONAL_ID_CONFIG, "kafka-test-producer",
      ACKS_CONFIG, "all",
      ENABLE_IDEMPOTENCE_CONFIG, true,
      COMPRESSION_TYPE_CONFIG, CompressionType.LZ4.name
    );
    return new KafkaProducer<>(props);
  }
}
