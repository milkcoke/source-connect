package sourceconnector.domain.connect;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import sourceconnector.repository.file.LocalFileRepository;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkerTest {
  private final Properties producerProperties = new Properties();

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
    assertThatThrownBy(() -> worker.createTasks(0, 1, new LocalFileRepository(), producerProperties
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
    assertThatThrownBy(() -> worker.createTasks(1, 0, new LocalFileRepository(), producerProperties))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Total task count should be greater than zero");
  }

  @DisplayName("Success to create two tasks in the worker")
  @Test
  void createTwoTasksTest() {
    // given
    Worker worker = new Worker(
      0,
      new FileTaskAssignor(List.of("file-0", "file-1"), 2)
    );
    // when
    Collection<Task<FileProcessingResult>> tasks = worker.createTasks(1, 2, new LocalFileRepository(), producerProperties);

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
  void successToStart() throws ExecutionException, InterruptedException {
    // given
    Worker worker = new Worker(
      0,
      new FileTaskAssignor(List.of("file-0", "file-1"), 2)
    );
    worker.createTasks(1, 2, new LocalFileRepository(), producerProperties);

    // when then
    assertDoesNotThrow(worker::start);
  }
}
