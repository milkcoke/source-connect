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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1)
public class WorkerBenchmark {
  private final List<String> testFilePaths = new ArrayList<>();
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
    final FileLister fileLister = new LocalFileLister(
      new CompositeFileValidator(Collections.singletonList(
        new FileExtensionFilter(List.of(".ndjson")))
      )
    );
    testFilePaths.addAll(fileLister.listFiles(false, testDirectory.toFile().getAbsolutePath()));
  }

  @Benchmark
  public void singleTaskBenchmark() throws ExecutionException, InterruptedException {
    Worker worker = new Worker(0, new FileTaskAssignor(this.testFilePaths, 1));
    worker.createTasks(1, 1, new LocalFileRepository(), this.producerConfig);
    worker.start();
  }


  @Benchmark
  public void fiveTaskBenchmark() throws ExecutionException, InterruptedException {
    Worker worker = new Worker(0, new FileTaskAssignor(this.testFilePaths, 5));
    worker.createTasks(1, 5, new LocalFileRepository(), this.producerConfig);
    worker.start();
  }

}
