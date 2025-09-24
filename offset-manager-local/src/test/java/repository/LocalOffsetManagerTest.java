package repository;

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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
      System.out.println(exception.getMessage());
    }

    consumerConfig.putAll(Map.of(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class,
      ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.name().toLowerCase(),
      ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 57_671_680, // 55MB
      ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5
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

  @DisplayName("Should update all offsets correctly when initialized")
  @Test
  void init() {
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

}
