package repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
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

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocalOffsetManagerTest {

  private final String offsetTopic = "offset-topic";
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

  @Order(1)
  @DisplayName("OffsetManager return empty when no offset found")
  @Test
  void returnEmptyWhenFirstCreatedTopic() {
    // given
    Consumer<String, Long> consumer = new KafkaConsumer<>(this.consumerConfig);
    LocalOffsetManager localOffsetManager = new LocalOffsetManager(consumer, this.offsetTopic);
    // when
    Optional<Long> foundOffset = localOffsetManager.findLatestOffset("key1");
    // then
    assertThat(foundOffset).isEmpty();
  }

  @Order(2)
  @DisplayName("Should update all offsets correctly when initialized")
  @Test
  void retrieveAllLastOffset() {
    // given
    Producer<String, Long> producer = new KafkaProducer<>(producerConfig);
    String keyA = "file-a.txt";
    String keyB = "file-b.txt";
    String keyC = "file-c.txt";
    producer.initTransactions();
    producer.beginTransaction();
    for (long i = 0; i < 5; i++) {
      producer.send(new ProducerRecord<>(this.offsetTopic, keyA, i));
      producer.send(new ProducerRecord<>(this.offsetTopic, keyB, i));
      producer.send(new ProducerRecord<>(this.offsetTopic, keyC, i));
    }
    producer.flush();
    producer.commitTransaction();
    producer.close();
    Consumer<String, Long> consumer = new KafkaConsumer<>(this.consumerConfig);

    // when
    LocalOffsetManager localOffsetManager = new LocalOffsetManager(consumer, this.offsetTopic);

    // then
    assertThat(localOffsetManager.findLatestOffset(keyA)).isPresent().contains(4L);
    assertThat(localOffsetManager.findLatestOffset(keyB)).isPresent().contains(4L);
    assertThat(localOffsetManager.findLatestOffset(keyC)).isPresent().contains(4L);
  }


  @Order(3)
  @DisplayName("Should update all offsets correctly even though transaction COMMIT Markers are interleaved")
  @Test
  void retrieveAllLastOffsetMany() {
    // given
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
    producer.close();

    Consumer<String, Long> consumer = new KafkaConsumer<>(this.consumerConfig);

    // when
    LocalOffsetManager localOffsetManager = new LocalOffsetManager(consumer, this.offsetTopic);

    // then
    assertThat(localOffsetManager.findLatestOffset(keyA)).isPresent().contains(1000L);
    assertThat(localOffsetManager.findLatestOffset(keyB)).isPresent().contains(1000L);
    assertThat(localOffsetManager.findLatestOffset(keyC)).isPresent().contains(1000L);
  }

}
