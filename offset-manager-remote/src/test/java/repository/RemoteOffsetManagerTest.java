package repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.OffsetManager;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.kafka.config.TopicBuilder;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RemoteOffsetManagerTest {

  private final String offsetTopic = "remote-offset-topic";
  private final Properties producerConfig = new Properties();
  private final Properties consumerConfig = new Properties();

  @BeforeAll
  void setup() throws ExecutionException, InterruptedException {
    producerConfig.putAll(Map.of(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        ProducerConfig.ACKS_CONFIG, "-1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class,
        ProducerConfig.LINGER_MS_CONFIG, 100,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
        ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-local"
      )
    );

    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");

    AdminClient adminClient = AdminClient.create(adminProps);
    NewTopic testTopic = TopicBuilder.name(this.offsetTopic)
      .compact()
      .partitions(3)
      .replicas(3)
      .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
      .config(TopicConfig.SEGMENT_MS_CONFIG, "10000")
      .build();

    try {
      adminClient.createTopics(List.of(testTopic)).all().get();
    } catch (TopicExistsException exception) {
      log.error(exception.getMessage(), exception);
    }

    consumerConfig.putAll(Map.of(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class,
      ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.name().toLowerCase(),
      ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 57_671_680, // 55MB
      ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500
    ));

    Thread.sleep(6_000);
  }

  @AfterAll
  void teardown() throws ExecutionException, InterruptedException {
    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
    AdminClient adminClient = AdminClient.create(adminProps);
    adminClient.deleteTopics(Collections.singleton(this.offsetTopic)).all().get();
    adminClient.close();
  }

  @DisplayName("Nothing to do update but background update thread starts")
  @Test
  void initializeTest() {
    // given
    OffsetManager offsetManager = new RemoteOffsetManager(new KafkaConsumer<>(consumerConfig), this.offsetTopic);
    // when
    Optional<Long> foundOffset = offsetManager.findLatestOffset("anyKey");
    // then
    assertThat(foundOffset).isEmpty();
  }

  @SneakyThrows(InterruptedException.class)
  @DisplayName("Update continuously receives new offsets and updates the store")
  @Test
  void updateContinuously() {
    // given
    OffsetManager offsetManager = new RemoteOffsetManager(new KafkaConsumer<>(consumerConfig), this.offsetTopic);
    assertThat(offsetManager.findLatestOffset("keyA")).isEmpty();
    assertThat(offsetManager.findLatestOffset("keyB")).isEmpty();
    assertThat(offsetManager.findLatestOffset("keyC")).isEmpty();

    Producer<String, Long> producer = new KafkaProducer<>(producerConfig);
    String keyA = "many-a.txt";
    String keyB = "many-b.txt";
    String keyC = "many-c.txt";
    producer.initTransactions();
    for (long i = 1; i <= 1000; i++) {
      if ((i - 1) % 100 == 0) {
        producer.beginTransaction();
      }
      producer.send(new ProducerRecord<>(this.offsetTopic, keyA, i));
      producer.send(new ProducerRecord<>(this.offsetTopic, keyB, i));
      producer.send(new ProducerRecord<>(this.offsetTopic, keyC, i));
      if (i % 100 == 0) {
        producer.commitTransaction();
      }
    }
    // when then
    Thread.sleep(1000);
    assertThat(offsetManager.findLatestOffset(keyA).get()).isEqualTo(1000L);
    assertThat(offsetManager.findLatestOffset(keyB).get()).isEqualTo(1000L);
    assertThat(offsetManager.findLatestOffset(keyC).get()).isEqualTo(1000L);
  }
}
