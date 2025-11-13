package sourceconnector.domain.connect;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.openjdk.jmh.annotations.*;
import sourceconnector.domain.file.FileKey;
import sourceconnector.domain.file.LocalFileKey;
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
  private Properties producerConfig;

  @Setup(Level.Trial)
  public void setup() throws IOException {
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

    Path testDirectory = Paths.get("src/jmh/resources/large-testdata");
    FileKey fileKey = LocalFileKey.from(testDirectory);

    final FileLister fileLister = new LocalFileLister(
      new CompositeFileValidator(Collections.singletonList(
        new FileExtensionFilter(List.of(".ndjson")))
      )
    );
    testFileKeys.addAll(fileLister.listFiles(fileKey));
  }

  @Benchmark
  public void singleTaskBenchmark() throws ExecutionException, InterruptedException {
    Worker worker = new Worker(0, new FileTaskAssignor(this.testFileKeys, 1));
    worker.createTasks(1, 1, pipelineSupplier, this.producerConfig, "log-topic", "offset-topic");
    worker.start();
  }


  @Benchmark
  public void fiveTaskBenchmark() throws ExecutionException, InterruptedException {
    Worker worker = new Worker(0, new FileTaskAssignor(this.testFileKeys, 5));
    worker.createTasks(1, 5, pipelineSupplier, this.producerConfig, "log-topic", "offset-topic");
    worker.start();
  }

}
