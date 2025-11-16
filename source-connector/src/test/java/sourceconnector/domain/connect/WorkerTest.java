package sourceconnector.domain.connect;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import sourceconnector.domain.file.FileKey;
import sourceconnector.domain.file.LocalFileKey;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder;
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;
import sourceconnector.domain.processor.impl.EmptyFilterProcessor;
import sourceconnector.domain.processor.impl.TrimMapperProcessor;
import sourceconnector.repository.file.LocalFileRepository;

import java.nio.file.Path;
import java.util.*;

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

  @BeforeAll
  void setUp() {
    producerProperties.putAll(Map.of(
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class
    ));
  }

  @DisplayName("Should throw IllegalArgumentException when worker count is 0")
  @Test
  void createNoWorkerTest() {
    // given
    Worker worker = new Worker(0, new FileTaskAssignor(Collections.emptyList(), 0));

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
    Worker worker = new Worker(0, new FileTaskAssignor(Collections.emptyList(), 0));

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
      new FileTaskAssignor(List.of(fileKey1, fileKey2), 2)
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
    Worker worker = new Worker(0, new FileTaskAssignor(Collections.emptyList(), 0));

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
        2)
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
