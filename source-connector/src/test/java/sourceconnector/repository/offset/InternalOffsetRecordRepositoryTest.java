package sourceconnector.repository.offset;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.service.offset.OffsetRecordRepository;
import sourceconnector.support.KafkaTestSupport;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InternalOffsetRecordRepositoryTest extends KafkaTestSupport {
  private final String offsetTopicName = "internal-offset-test";
  private KafkaProducer<String, byte[]> producer;

  @BeforeAll
  void setup() {
    createOffsetTopic(offsetTopicName, 2);
    this.producer = createProducer();
    this.producer.initTransactions();
  }

  @AfterAll
  void teardown() {
    producer.close();
  }


  @DisplayName("Should empty when no offset record exists for the given FileKey")
  @Test
  void notFoundOffsetTest() throws Exception {
    // given
    FileKey notExistFileKey = LocalFileKey.from(Path.of("NotExistFile.ndjson"));
    KafkaConsumer<String, Long> consumer = createConsumer();
    try (OffsetRecordRepository repository = new InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName)
    ) {
      // when
      Optional<OffsetRecord> offsetRecord = repository.findLastOffsetRecord(notExistFileKey);
      // then
      assertThat(offsetRecord).isEmpty();
    };
  }

  @DisplayName("Get last offset record for a FileKey")
  @Test
  void findLastOffsetRecord() throws Exception {
    // given
    FileKey fileKey = FileKeyParser.parse("file:///sample-data.ndjson");

    producer.beginTransaction();
    for (long offset = 0; offset <= 100; offset++) {
      OffsetRecord record = new DefaultOffsetRecord(fileKey, offset);

      producer.send(new ProducerRecord<>(
        this.offsetTopicName,
        record.key().get(),
        ByteBuffer.allocate(Long.BYTES).putLong(record.offset()).array()
      ));
    }
    producer.commitTransaction();
    KafkaConsumer<String, Long> consumer = createConsumer();
    AdminClient adminClient = createAdminClient();

    try (OffsetRecordRepository repository = new InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
    )) {
      // when
      Optional<OffsetRecord> offsetRecord = repository.findLastOffsetRecord(fileKey);
      // then
      assertThat(offsetRecord)
        .hasValueSatisfying(record -> {
          assertThat(record.key().get()).isEqualTo(fileKey.get());
          assertThat(record.offset()).isEqualTo(100L);
        });
    }
  }

  @DisplayName("Get last offset record regardless the offset numbering for a FileKey")
  @Test
  void findLastOffsetReverseOffsetValueTest() throws Exception {
    // given
    FileKey fileKey = FileKeyParser.parse("file:///reverse-data.ndjson");

    producer.beginTransaction();
    for (long offset = 100; offset >= 0; offset--) {
      OffsetRecord record = new DefaultOffsetRecord(fileKey, offset);

      producer.send(new ProducerRecord<>(
        this.offsetTopicName,
        record.key().get(),
        ByteBuffer.allocate(Long.BYTES).putLong(record.offset()).array()
      ));
    }
    producer.commitTransaction();

    KafkaConsumer<String, Long> consumer = createConsumer();
    AdminClient adminClient = createAdminClient();

    try (OffsetRecordRepository repository = new InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
      )) {
      // when
      Optional<OffsetRecord> offsetRecord = repository.findLastOffsetRecord(fileKey);

      // then
      assertThat(offsetRecord)
        .hasValueSatisfying(record -> {
          assertThat(record.key().get()).isEqualTo(fileKey.get());
          assertThat(record.offset()).isEqualTo(0L);
        });
    }
  }

  @DisplayName("Should return empty list when no offset records exist for the given FileKeys")
  @Test
  void notFoundOffsetsTest() throws Exception {
    // given
    List<FileKey> notExistFileKeys = List.of(
      LocalFileKey.from(Path.of("NotExistFile1.ndjson")),
      LocalFileKey.from(Path.of("NotExistFile2.ndjson")),
      LocalFileKey.from(Path.of("NotExistFile3.ndjson"))
    );
    KafkaConsumer<String, Long> consumer = createConsumer();
    AdminClient adminClient = createAdminClient();

    try (OffsetRecordRepository repository = new InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName)
    ) {
      // when
      List<OffsetRecord> offsetRecords = repository.findLastOffsetRecords(notExistFileKeys);
      // then
      assertThat(offsetRecords).isEmpty();
    }
  }

  @DisplayName("Get last offset records for multiple FileKeys")
  @Test
  void findLastOffsetRecords() throws Exception {
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
          this.offsetTopicName,
          record.key().get(),
          ByteBuffer.allocate(Long.BYTES).putLong(record.offset()).array()
        ));
      }
    }
    producer.commitTransaction();

    KafkaConsumer<String, Long> consumer = createConsumer();
    AdminClient adminClient = createAdminClient();

    try (OffsetRecordRepository repository = new InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
    )) {
      // when
      List<OffsetRecord> offsetRecords = repository.findLastOffsetRecords(fileKeys);
      // then
      assertThat(offsetRecords).hasSize(fileKeys.size())
        .containsExactlyInAnyOrder(
          new DefaultOffsetRecord(fileKeys.get(0), 100L),
          new DefaultOffsetRecord(fileKeys.get(1), 100L),
          new DefaultOffsetRecord(fileKeys.get(2), 100L)
        );
    }
  }

  @DisplayName("Get last offset records in reverse")
  @Test
  void findLastOffsetsReverseOffsetValueTest() throws Exception {
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
          this.offsetTopicName,
          record.key().get(),
          ByteBuffer.allocate(Long.BYTES).putLong(record.offset()).array()
        ));
      }
    }
    producer.commitTransaction();

    KafkaConsumer<String, Long> consumer = createConsumer();
    AdminClient adminClient = createAdminClient();

    try (OffsetRecordRepository repository = new InternalOffsetRecordRepository(
      consumer,
      adminClient,
      this.offsetTopicName
    )) {
      // when
      List<OffsetRecord> offsetRecords = repository.findLastOffsetRecords(fileKeys);
      // then
      assertThat(offsetRecords).hasSize(fileKeys.size())
        .containsExactlyInAnyOrder(
          new DefaultOffsetRecord(fileKeys.get(0), -1L),
          new DefaultOffsetRecord(fileKeys.get(1), -1L),
          new DefaultOffsetRecord(fileKeys.get(2), -1L)
        );
    }
  }

}
