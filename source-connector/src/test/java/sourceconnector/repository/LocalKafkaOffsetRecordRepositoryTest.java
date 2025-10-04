package sourceconnector.repository;

import offsetmanager.domain.OffsetRecord;
import offsetmanager.domain.OffsetStatus;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.kafka.config.TopicBuilder;
import sourceconnector.domain.offset.LocalFileOffsetRecord;
import sourceconnector.repository.offset.v1.LocalKafkaOffsetRecordRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocalKafkaOffsetRecordRepositoryTest {
  private final String testTopicName = "offset-topic";
  private LocalKafkaOffsetRecordRepository repository;
  private static final Properties props = new Properties();
  static {
    props.putAll(Map.of(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        ProducerConfig.ACKS_CONFIG, "-1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class,
        ProducerConfig.LINGER_MS_CONFIG, 100,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
      )
    );
  }

  @BeforeAll
  void setup() throws ExecutionException, InterruptedException {
    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");

    AdminClient adminClient = AdminClient.create(adminProps);
    NewTopic testTopic = TopicBuilder.name(this.testTopicName)
      .compact()
      .partitions(1)
      .replicas(3)
      .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
      .config(TopicConfig.SEGMENT_MS_CONFIG, "10000")
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

    repository = new LocalKafkaOffsetRecordRepository(consumer, adminClient);
    Thread.sleep(5_000);
  }

  @AfterAll
  void teardown() throws ExecutionException, InterruptedException {
    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
    AdminClient adminClient = AdminClient.create(adminProps);
    adminClient.deleteTopics(Collections.singleton(this.testTopicName)).all().get();
    adminClient.close();
  }

  @DisplayName("Should get initial offset value when no record found")
  @Test
  void foundNoRecordTest() {
    // given when
    OffsetRecord offsetRecord = this.repository.findLastOffsetRecord(this.testTopicName, "NotExistFile.ndjson");
    // then
    assertThat(offsetRecord)
      .hasFieldOrPropertyWithValue("key", "NotExistFile.ndjson")
      .hasFieldOrPropertyWithValue("offset", OffsetStatus.INITIAL.getValue());
  }

  @DisplayName("Should get the last offset value when local file path exists in the offset topic")
  @Test
  void getLastRecordTest() {
    // given
    String filePath = "src/test/resources/sample-data/empty-included.ndjson";

    try (KafkaProducer<String, Long> producer = new KafkaProducer<>(props)) {

      for (long offset = 0; offset <= 100; offset++) {
        OffsetRecord record = new LocalFileOffsetRecord(filePath, offset);

        producer.send(new ProducerRecord<>(
          this.testTopicName,
          record.key(),
          record.offset()
        ));
      }
    } catch (Exception ignored) {
    }

    // when
    OffsetRecord offsetRecord = this.repository.findLastOffsetRecord(this.testTopicName, filePath);

    // then
    assertThat(offsetRecord.key()).isEqualTo(filePath);
    assertThat(offsetRecord.offset()).isEqualTo(100L);
  }
}
