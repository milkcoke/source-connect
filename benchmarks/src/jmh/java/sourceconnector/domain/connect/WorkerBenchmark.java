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
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.openjdk.jmh.annotations.*;
import org.springframework.kafka.config.TopicBuilder;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder;
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;
import sourceconnector.repository.file.FileLister;
import sourceconnector.repository.file.LocalFileLister;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.repository.file.filter.FileExtensionFilter;
import sourceconnector.repository.file.validator.CompositeFileValidator;
import sourceconnector.repository.offset.InternalOffsetRecordRepository;
import sourceconnector.service.offset.OffsetRecordServiceImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1)
public class WorkerBenchmark {
  private final List<FileKey> testFileKeys = new ArrayList<>();
  private final PipelineSupplier<Log> pipelineSupplier = new FileLogPipelineSupplier(
    new FileBaseLogPipelineBuilder(),
    new LocalFileRepository(),
    new JSONLogFactory(),
    Collections::emptyList
  );
  private OffsetRecordService offsetRecordService;
  private Properties producerConfig;
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

  @Setup(Level.Trial)
  public void setup() throws IOException {
    Path testDirectory = Paths.get("src/jmh/resources/large-testdata");
    FileKey fileKey = LocalFileKey.from(testDirectory);

    final FileLister fileLister = new LocalFileLister(
      new CompositeFileValidator(Collections.singletonList(
        new FileExtensionFilter(List.of(".ndjson")))
      )
    );
    testFileKeys.addAll(fileLister.listFiles(fileKey));

    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");

    AdminClient adminClient = AdminClient.create(adminProps);
    try {
      adminClient.createTopics(List.of(this.logTopic, this.offsetTopic)).all().get();
    } catch (TopicExistsException ignored) {
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    Properties consumerProps = new Properties();
    consumerProps.putAll(Map.of(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
      ConsumerConfig.GROUP_ID_CONFIG, "benchmark-offset-consumer",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class
    ));

    this.offsetRecordService = new OffsetRecordServiceImpl(
      new InternalOffsetRecordRepository(
        new KafkaConsumer<>(consumerProps),
        adminClient,
        offsetTopic.name())
    );

    Properties properties = new Properties();
    properties.putAll(Map.of(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        ProducerConfig.ACKS_CONFIG, "-1",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class,
        ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4",
        ProducerConfig.LINGER_MS_CONFIG, 100,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
      )
    );
    this.producerConfig = properties;

  }

  @Benchmark
  public void singleTaskBenchmark() throws ExecutionException, InterruptedException {
    Worker worker = new Worker(0, new FileTaskAssignor(this.testFileKeys, 1, this.offsetRecordService));
    worker.createTasks(
      1, 1,
      pipelineSupplier, this.producerConfig,
      logTopic.name(), offsetTopic.name()
    );
    worker.start();
  }


  @Benchmark
  public void fiveTaskBenchmark() throws ExecutionException, InterruptedException {
    Worker worker = new Worker(0, new FileTaskAssignor(this.testFileKeys, 5, this.offsetRecordService));
    worker.createTasks(
      1, 5,
      pipelineSupplier, this.producerConfig,
      logTopic.name(), offsetTopic.name()
    );
    worker.start();
  }

}
