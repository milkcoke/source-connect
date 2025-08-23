package sourceconnector.repository;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.TopicBuilder;
import sourceconnector.domain.OffsetRecord;
import sourceconnector.domain.OffsetStatus;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class S3OffsetRepositoryTest {
  private static S3OffsetRepository repository;
  private static final Properties props = new Properties();
  static {
    props.putAll(Map.of(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "-1",
        org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class,
        org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, 100,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
//        ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-s3"
      )
    );
  }

  @BeforeEach
  void setup() throws ExecutionException, InterruptedException {
    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");

    AdminClient adminClient = AdminClient.create(adminProps);
    NewTopic testTopic = TopicBuilder.name("test-offset")
      .compact()
      .partitions(1)
      .replicas(3)
      .config("min.insync.replicas", "2")
      .config("segment.ms", "10000")
      .build();

    adminClient.createTopics(List.of(testTopic)).all().get();

    Properties consumerProps = new Properties();
    consumerProps.putAll(Map.of(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class,
      ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 57_671_680, // 55MB
      ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50_000
    ));
    Consumer<String, Long> consumer = new KafkaConsumer<>(consumerProps);

    repository = new S3OffsetRepository(consumer, adminClient);
  }

  @AfterEach
  void teardown() throws ExecutionException, InterruptedException {
    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
    AdminClient adminClient = AdminClient.create(adminProps);
    adminClient.deleteTopics(Collections.singleton("test-offset")).all().get();
    adminClient.close();
  }

  @DisplayName("Get all offset within range")
  @Test
  void testForwardScanLastRecord(){
    // given

    try (KafkaProducer<String, Long> producer = new KafkaProducer<>(props)) {
      for (long i = 0; i < 10_000; i++) {
        producer.send(new ProducerRecord<>("test-offset", String.valueOf(i / 1000), i));
      }
      producer.flush();
      Thread.sleep(Duration.ofSeconds(10L));
      producer.send(new ProducerRecord<>("test-offset", String.valueOf(10_000), 10_000L));

      producer.flush();
    } catch (Exception ignored) {
      System.out.printf(ignored.getMessage());
    }

    // when
    OffsetRecord lastOffsetRecord = repository.getLastOffsetRecord("test-offset", "10000");
    // then
    assertThat(lastOffsetRecord)
      .extracting(OffsetRecord::key, OffsetRecord::offset)
      .containsExactly(
        "10000",
        10000L
      );

  }

  @DisplayName("Should get INITIAL Offset when not processed key ")
  @Test
  void getLastOffsetRecord() {
     OffsetRecord lastOffset = repository.getLastOffsetRecord(
      "s3-offset-topic",
      "s3://test/2025/04/13/test.txt"
    );

    assertThat(lastOffset)
      .extracting(OffsetRecord::key, OffsetRecord::offset)
      .containsExactly(
        "s3://test/2025/04/13/test.txt",
        OffsetStatus.INITIAL_OFFSET.getValue()
      );
  }

  @DisplayName("Should always get same partition when same key is input")
  @Test
  void getPartitionsForTopic() {
    // given
    String offsetTopic = "s3-offset-topic";

    int partition1 = repository.getPartitionsForTopic(offsetTopic, "s3://test/2025/04/13/test.txt");
    int partition2 = repository.getPartitionsForTopic(offsetTopic, "s3://test/2025/04/13/test.txt");
    int partition3 = repository.getPartitionsForTopic(offsetTopic, "s3://test/2025/04/13/test.txt");

    assertThat(List.of(partition1, partition2, partition3))
      .containsOnly(partition1);
  }
}
