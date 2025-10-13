package sourceconnector.domain.connect;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.openjdk.jmh.annotations.*;
import sourceconnector.repository.file.FileLister;
import sourceconnector.repository.file.LocalFileLister;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.repository.file.filter.FileExtensionFilter;
import sourceconnector.repository.file.validator.CompositeFileValidator;
import sourceconnector.service.producer.BatchProduceService;
import sourceconnector.service.producer.BatchProducer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 0)
public class FileSourceTaskBenchmark {
  private BatchProducer<String> batchProducer;
  private final FileLister fileLister = new LocalFileLister(
    new CompositeFileValidator(Collections.singletonList(
      new FileExtensionFilter(List.of(".ndjson")))
    )
  );
  private final List<String> testFilePaths = new ArrayList<>();

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
      ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
      ProducerConfig.TRANSACTIONAL_ID_CONFIG, "single-task-"
      )
    );

    this.batchProducer = new BatchProduceService(properties, "log-topic", "offset-topic");

    Path testDirectory = Paths.get("src/jmh/resources/testdata");
    testFilePaths.addAll(fileLister.listFiles(false, testDirectory.toFile().getAbsolutePath()));
  }

  @Benchmark
  public FileProcessingResult singleTaskBenchmark() throws Exception {
    Task<FileProcessingResult> task = new FileSourceTask(
      0,
      new LocalFileRepository(),
      this.batchProducer
    );
    task.assign(this.testFilePaths);
    return task.call();
  }
}
