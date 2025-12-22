package offsetmanager.repository;

import offsetmanager.KafkaTestSupport;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetRecord;
import offsetmanager.manager.OffsetManager;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OffsetManagerRepositoryTest extends KafkaTestSupport {

  private final String offsetTopic = "remote-offset-topic";
  private Producer<String, Long> producer;

  @BeforeAll
  void setup() {
    this.producer = createProducer();
    producer.initTransactions();
    createTestTopic(offsetTopic, 3);
  }

  @AfterAll
  void teardown() {
    producer.close();
  }

  @DisplayName("Nothing to do update but background update thread starts")
  @Test
  void emptyOffsetTopicTest() {
    // given
    KafkaConsumer<String, Long> consumer = createConsumer();
    OffsetManager offsetManager = new OffsetManagerRepository(consumer, this.offsetTopic);
    // when
    Optional<OffsetRecord> foundOffset = offsetManager.findLatestOffsetRecord(FileKeyParser.parse("file:///test/path.txt"));
    // then
    assertThat(foundOffset).isEmpty();
  }

  @DisplayName("Should find all offset records by keys")
  @Test
  void findAllOffsetRecordsTest() throws InterruptedException {
    // given
    KafkaConsumer<String, Long> consumer = createConsumer();
    OffsetManager offsetManager = new OffsetManagerRepository(consumer, this.offsetTopic);
    FileKey keyA = FileKeyParser.parse("file:///many-a.txt");
    FileKey keyB = FileKeyParser.parse("file:///many-b.txt");
    FileKey keyC = FileKeyParser.parse("file:///many-c.txt");
    for (long i = 1; i <= 1000; i++) {
      if ((i - 1) % 100 == 0) {
        producer.beginTransaction();
      }
      producer.send(new ProducerRecord<>(this.offsetTopic, keyA.get(), i));
      producer.send(new ProducerRecord<>(this.offsetTopic, keyB.get(), i));
      producer.send(new ProducerRecord<>(this.offsetTopic, keyC.get(), i));
      if (i % 100 == 0) {
        producer.commitTransaction();
      }
    }
    Thread.sleep(1000);

    // when
    List<OffsetRecord> offsetRecords = offsetManager.findLatestOffsetRecords(List.of(keyA, keyB, keyC));
    // then
    assertThat(offsetRecords)
      .hasSize(3)
      .containsExactlyInAnyOrder(
        new DefaultOffsetRecord(keyA, 1000L),
        new DefaultOffsetRecord(keyB, 1000L),
        new DefaultOffsetRecord(keyC, 1000L)
      );

  }

  @DisplayName("Update continuously receives new offsets and updates the store")
  @Test
  void upsertContinuously() throws InterruptedException {
    // given
    KafkaConsumer<String, Long> consumer = createConsumer();
    OffsetManager offsetManager = new OffsetManagerRepository(consumer, this.offsetTopic);
    FileKey keyA = FileKeyParser.parse("file:///key-a.txt");
    FileKey keyB = FileKeyParser.parse("file:///key-b.txt");
    FileKey keyC = FileKeyParser.parse("file:///key-c.txt");
    assertThat(offsetManager.findLatestOffsetRecord(keyA)).isEmpty();
    assertThat(offsetManager.findLatestOffsetRecord(keyB)).isEmpty();
    assertThat(offsetManager.findLatestOffsetRecord(keyC)).isEmpty();

    for (long i = 1; i <= 1000; i++) {
      if ((i - 1) % 100 == 0) {
        producer.beginTransaction();
      }
      producer.send(new ProducerRecord<>(this.offsetTopic, keyA.get(), i));
      producer.send(new ProducerRecord<>(this.offsetTopic, keyB.get(), i));
      producer.send(new ProducerRecord<>(this.offsetTopic, keyC.get(), i));
      if (i % 100 == 0) {
        producer.commitTransaction();
      }
    }
    // when then
    Thread.sleep(1000);
    assertThat(offsetManager.findLatestOffsetRecord(keyA).get())
      .isEqualTo(new DefaultOffsetRecord(keyA, 1000L));
    assertThat(offsetManager.findLatestOffsetRecord(keyB).get())
      .isEqualTo(new DefaultOffsetRecord(keyB, 1000L));
    assertThat(offsetManager.findLatestOffsetRecord(keyC).get())
      .isEqualTo(new DefaultOffsetRecord(keyC, 1000L));
  }
}
