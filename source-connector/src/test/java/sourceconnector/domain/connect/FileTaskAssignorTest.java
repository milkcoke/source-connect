package sourceconnector.domain.connect;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.file.FileKey;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.kafka.config.TopicBuilder;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder;
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.repository.offset.InternalOffsetRecordRepository;
import sourceconnector.service.offset.OffsetRecordServiceImpl;
import sourceconnector.service.producer.BatchProduceService;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileTaskAssignorTest {
  private final Properties producerProperties = new Properties();
  private final PipelineSupplier<Log> pipelineSupplier = new FileLogPipelineSupplier(
    new FileBaseLogPipelineBuilder(),
    new LocalFileRepository(),
    new JSONLogFactory(),
    Collections::emptyList
  );
  private OffsetRecordService offsetRecordService;

  private final NewTopic offsetTopic = TopicBuilder.name("test-offset")
    .compact()
    .partitions(1)
    .replicas(3)
    .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
    .config(TopicConfig.SEGMENT_MS_CONFIG, "10000")
    .build();
  private final NewTopic logTopic = TopicBuilder.name("test-log")
    .partitions(3)
    .replicas(3)
    .build();

  @BeforeAll
  void setUp() {
    producerProperties.putAll(Map.of(
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class,
      ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-task-"
    ));

    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");

    AdminClient adminClient = AdminClient.create(adminProps);
    try {
      adminClient.createTopics(List.of(this.logTopic, this.offsetTopic)).all().get();
    } catch (InterruptedException | ExecutionException ignored) {
    }

    Properties consumerProps = new Properties();
    consumerProps.putAll(Map.of(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
      ConsumerConfig.GROUP_ID_CONFIG, "benchmark-offset-consumer",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class
    ));

    this.offsetRecordService = new OffsetRecordServiceImpl(
      new InternalOffsetRecordRepository(
        new KafkaConsumer<>(consumerProps),
        adminClient,
        offsetTopic.name())
    );
  }

  @RequiredArgsConstructor
  static class FakeFileKey implements FileKey {
    private final String path;
    @Override
    public String get() {
      return "";
    }
  }

  @DisplayName("First task has the number of 6 file paths when task 5 and file paths 30 provided")
  @Test
  void assignedFileCountTest() {

    // given
    List<FileKey> fileKeys = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      fileKeys.add(new FakeFileKey("file-" + i ));
    }
    TaskAssignor taskAssignor = new FileTaskAssignor(fileKeys, 5, this.offsetRecordService);
    FileSourceTask task0 = new FileSourceTask(
      0, pipelineSupplier,
      new BatchProduceService(producerProperties, this.offsetTopic.name(), this.logTopic.name()));

    // when
    taskAssignor.assign(List.of(task0));

    // then
    assertThat(task0.getFileKeyOffsetMap())
      .hasSize(6);
  }

  @DisplayName("First and second task have the number of 8 file paths when task 4 and file paths 30 provided")
  @Test
  void assignedFileCountTest2() {
    // given
    List<FileKey> fileKeys = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      fileKeys.add(new FakeFileKey("file-" + i));
    }
    TaskAssignor taskAssignor = new FileTaskAssignor(fileKeys, 4, this.offsetRecordService);
    FileSourceTask task0 = new FileSourceTask(0, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic.name(), this.offsetTopic.name()));
    FileSourceTask task1 =  new FileSourceTask(1, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic.name(), this.offsetTopic.name()));
    FileSourceTask task2 =  new FileSourceTask(2, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic.name(), this.offsetTopic.name()));
    FileSourceTask task3 =   new FileSourceTask(3, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic.name(), this.offsetTopic.name()));

    Collection<Task<FileProcessingResult>> tasks = List.of(task0, task1, task2, task3);

    // when
    taskAssignor.assign(tasks);

    // then
    assertAll(
      ()->assertThat(task0.getFileKeyOffsetMap()).hasSize(8),
      ()->assertThat(task1.getFileKeyOffsetMap()).hasSize(8),
      ()->assertThat(task2.getFileKeyOffsetMap()).hasSize(7),
      ()->assertThat(task3.getFileKeyOffsetMap()).hasSize(7)
    );
  }

  @DisplayName("Can assign even though file path has no element")
  @Test
  void emptyFilePathAssignTest() {
    // given
    TaskAssignor taskAssignor = new FileTaskAssignor(Collections.emptyList(), 1, this.offsetRecordService);
    Collection<Task<FileProcessingResult>> tasks = List.of(
      new FileSourceTask( 0, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic.name(), this.offsetTopic.name()))
    );

    // when then
    assertDoesNotThrow(() -> taskAssignor.assign(tasks));
  }

}
