package sourceconnector.domain.connect;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.record.CompressionType;
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
import sourceconnector.domain.processor.impl.EmptyFilterProcessor;
import sourceconnector.domain.processor.impl.TrimMapperProcessor;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.repository.offset.InternalOffsetRecordRepository;
import sourceconnector.service.offset.OffsetRecordServiceImpl;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkerTest {
  private final Properties producerProperties = new Properties();
  private final PipelineSupplier<Log> pipelineSupplier = new FileLogPipelineSupplier(
    new FileBaseLogPipelineBuilder(),
    new LocalFileRepository(),
    new JSONLogFactory(),
    ()->List.of(new EmptyFilterProcessor(), new TrimMapperProcessor(new JSONLogFactory()))
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
      ProducerConfig.COMPRESSION_TYPE_CONFIG, CompressionType.LZ4.name,
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class
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

  @DisplayName("Should throw IllegalArgumentException when worker count is 0")
  @Test
  void createNoWorkerTest() {
    // given
    Worker worker = new Worker(0, new FileTaskAssignor(Collections.emptyList(), 0, this.offsetRecordService));

    // when then
    assertThatThrownBy(() -> worker.createTasks(
      0, 1,
      pipelineSupplier, producerProperties,
      "log-topic", "offset-topic"
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Total worker count should be greater than zero");
  }


  @DisplayName("Should throw IllegalArgumentException when task count is 0")
  @Test
  void createNoTasksTest() {
    // given
    Worker worker = new Worker(0, new FileTaskAssignor(Collections.emptyList(), 0, this.offsetRecordService));

    // when then
    assertThatThrownBy(() -> worker.createTasks(
      1, 0,
      pipelineSupplier, producerProperties,
      "log-topic", "offset-topic"
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Total task count should be greater than zero");
  }

  @DisplayName("Success to create two tasks in the worker")
  @Test
  void createTwoTasksTest() {
    // given
    FileKey fileKey1 = LocalFileKey.from(Path.of("file-0"));
    FileKey fileKey2 = LocalFileKey.from(Path.of("file-1"));

    Worker worker = new Worker(
      0,
      new FileTaskAssignor(List.of(fileKey1, fileKey2), 2, this.offsetRecordService)
    );
    // when
    Collection<Task<FileProcessingResult>> tasks = worker.createTasks(
      1, 2,
      pipelineSupplier, producerProperties,
      "log-topic", "offset-topic"
    );

    // then
    assertThat(tasks).hasSize(2);
  }


  @DisplayName("Should throw IllegalStateException when no tasks to start")
  @Test
  void NoTaskStartTest() {
    // given
    Worker worker = new Worker(0, new FileTaskAssignor(Collections.emptyList(), 0, this.offsetRecordService));

    // when then
    assertThatThrownBy(worker::start)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("No tasks to start");
  }

  @DisplayName("Success to start after that tasks are created")
  @Test
  void successToStart() {
    // given
    Path path1 = Path.of("src/test/resources/sample-data/subdir1/sub1.ndjson");
    Path path2= Path.of("src/test/resources/sample-data/empty.ndjson");

    Worker worker = new Worker(
      0,
      new FileTaskAssignor(List.of(
        LocalFileKey.from(path1),
        LocalFileKey.from(path2)
      ),
        2,
        this.offsetRecordService
      )
    );
    Collection<Task<FileProcessingResult>> tasks = worker.createTasks(
      1, 2,
      pipelineSupplier, producerProperties,
      "log-topic", "offset-topic"
    );

    // when then
    assertDoesNotThrow(worker::start);
  }
}
