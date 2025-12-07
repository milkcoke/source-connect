package sourceconnector.repository.offset;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetRecord;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
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
import sourceconnector.service.offset.OffsetRecordRepository;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InternalOffsetRecordRepositoryTest {
  private final String offsetTopic = "offset-topic";
  private OffsetRecordRepository repository;
  private static final KafkaProducer<String, Long> producer;
  static {
    final Properties props = new Properties();
    props.putAll(Map.of(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        ProducerConfig.ACKS_CONFIG, "-1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class,
        ProducerConfig.LINGER_MS_CONFIG, 100,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
        ProducerConfig.TRANSACTIONAL_ID_CONFIG, "offset-producer",
        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5
      )
    );
    producer = new KafkaProducer<>(props);
    producer.initTransactions();
  }

  @BeforeAll
  void setup() throws ExecutionException, InterruptedException {
    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");

    AdminClient adminClient = AdminClient.create(adminProps);
    NewTopic testTopic = TopicBuilder.name(this.offsetTopic)
      .compact()
      .partitions(1)
      .replicas(3)
      .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
      .config(TopicConfig.SEGMENT_MS_CONFIG, "10000")
      .build();

    try {
      adminClient.createTopics(List.of(testTopic)).all().get();
    } catch (TopicExistsException ignored) {}

    Properties consumerProps = new Properties();
    consumerProps.putAll(Map.of(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class,
      ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 57_671_680, // 55MB
      ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50_000,
      ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString()
    ));
    Consumer<String, Long> consumer = new KafkaConsumer<>(consumerProps);

    repository = new InternalOffsetRecordRepository(consumer, adminClient, offsetTopic);
    Thread.sleep(5_000);
  }

  @AfterAll
  void teardown() throws ExecutionException, InterruptedException {
    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
    AdminClient adminClient = AdminClient.create(adminProps);
    adminClient.deleteTopics(Collections.singleton(this.offsetTopic)).all().get();
    adminClient.close();
  }


  @DisplayName("Should empty when no offset record exists for the given FileKey")
  @Test
  void notFoundOffsetTest() {
    // given
    FileKey notExistFileKey = LocalFileKey.from(Path.of("NotExistFile.ndjson"));
    // when
    Optional<OffsetRecord> offsetRecord = this.repository.findLastOffsetRecord(notExistFileKey);
    // then
    assertThat(offsetRecord).isEmpty();
  }

  @DisplayName("Get last offset record for a FileKey")
  @Test
  void findLastOffsetRecord() {
    // given
    FileKey fileKey = FileKeyParser.parse("file:///sample-data.ndjson");

    producer.beginTransaction();
    for (long offset = 0; offset <= 100; offset++) {
      OffsetRecord record = new DefaultOffsetRecord(fileKey, offset);

      producer.send(new ProducerRecord<>(
        this.offsetTopic,
        record.key().get(),
        record.offset()
      ));
    }

    producer.commitTransaction();
    // when
    Optional<OffsetRecord> offsetRecord = this.repository.findLastOffsetRecord(fileKey);
    // then
    assertThat(offsetRecord)
      .hasValueSatisfying(record -> {
        assertThat(record.key().get()).isEqualTo(fileKey.get());
        assertThat(record.offset()).isEqualTo(100L);
      });
  }

  @DisplayName("Get last offset record regardless the offset numbering for a FileKey")
  @Test
  void findLastOffsetReverseOffsetValueTest() {
    // given
    FileKey fileKey = FileKeyParser.parse("file:///reverse-data.ndjson");

    producer.beginTransaction();
    for (long offset = 100; offset >= 0; offset--) {
      OffsetRecord record = new DefaultOffsetRecord(fileKey, offset);

      producer.send(new ProducerRecord<>(
        this.offsetTopic,
        record.key().get(),
        record.offset()
      ));
    }
    producer.commitTransaction();

    // when
    Optional<OffsetRecord> offsetRecord = this.repository.findLastOffsetRecord(fileKey);
    // then
    assertThat(offsetRecord)
      .hasValueSatisfying(record -> {
        assertThat(record.key().get()).isEqualTo(fileKey.get());
        assertThat(record.offset()).isEqualTo(0L);
      });
  }

  @DisplayName("Should return empty list when no offset records exist for the given FileKeys")
  @Test
  void notFoundOffsetsTest() {
    // given
    List<FileKey> notExistFileKeys = List.of(
      LocalFileKey.from(Path.of("NotExistFile1.ndjson")),
      LocalFileKey.from(Path.of("NotExistFile2.ndjson")),
      LocalFileKey.from(Path.of("NotExistFile3.ndjson"))
    );
    // when
    List<OffsetRecord> offsetRecords = this.repository.findLastOffsetRecords(notExistFileKeys);
    // then
    assertThat(offsetRecords).isEmpty();
  }

  @DisplayName("Get last offset records for multiple FileKeys")
  @Test
  void findLastOffsetRecords() {
    // given
    List<FileKey> fileKeys = List.of(
      FileKeyParser.parse("file:///sample-data1.ndjson"),
      FileKeyParser.parse("file:///sample-data2.ndjson"),
      FileKeyParser.parse("file:///sample-data3.ndjson")
    );

    producer.beginTransaction();
    for (FileKey fileKey : fileKeys) {
      for (long offset = 0; offset <= 100; offset++) {
        OffsetRecord record = new DefaultOffsetRecord(fileKey, offset);

        producer.send(new ProducerRecord<>(
          this.offsetTopic,
          record.key().get(),
          record.offset()
        ));
      }
    }
    producer.commitTransaction();

    // when
    List<OffsetRecord> offsetRecords = this.repository.findLastOffsetRecords(fileKeys);
    // then
    assertThat(offsetRecords).hasSize(fileKeys.size())
      .containsExactlyInAnyOrder(
        new DefaultOffsetRecord(fileKeys.get(0), 100L),
        new DefaultOffsetRecord(fileKeys.get(1), 100L),
        new DefaultOffsetRecord(fileKeys.get(2), 100L)
      );
  }

  @DisplayName("Get last offset records in reverse")
  @Test
  void findLastOffsetsReverseOffsetValueTest() {
    // given
    List<FileKey> fileKeys = List.of(
      FileKeyParser.parse("file:///sample-data1.ndjson"),
      FileKeyParser.parse("file:///sample-data2.ndjson"),
      FileKeyParser.parse("file:///sample-data3.ndjson")
    );

    producer.beginTransaction();
    for (FileKey fileKey : fileKeys) {
      for (long offset = 100; offset >= -1; offset--) {
        OffsetRecord record = new DefaultOffsetRecord(fileKey, offset);

        producer.send(new ProducerRecord<>(
          this.offsetTopic,
          record.key().get(),
          record.offset()
        ));
      }
    }
    producer.commitTransaction();

    // when
    List<OffsetRecord> offsetRecords = this.repository.findLastOffsetRecords(fileKeys);
    // then
    assertThat(offsetRecords).hasSize(fileKeys.size())
      .containsExactlyInAnyOrder(
        new DefaultOffsetRecord(fileKeys.get(0), -1L),
        new DefaultOffsetRecord(fileKeys.get(1), -1L),
        new DefaultOffsetRecord(fileKeys.get(2), -1L)
      );
  }

}
