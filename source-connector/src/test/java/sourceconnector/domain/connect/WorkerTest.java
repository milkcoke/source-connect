package sourceconnector.domain.connect;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import sourceconnector.support.KafkaTestSupport;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class WorkerTest extends KafkaTestSupport {
  private final PipelineSupplier<Log> pipelineSupplier = new FileLogPipelineSupplier(
    new FileBaseLogPipelineBuilder(),
    new LocalFileRepository(),
    new JSONLogFactory(),
    ()->List.of(new EmptyFilterProcessor(), new TrimMapperProcessor(new JSONLogFactory()))
  );

  private final String offsetTopic = "test-offset-topic";
  private final String logTopic = "test-log";

  private OffsetRecordService offsetRecordService;

  @BeforeAll
  void setup() {
    createOffsetTopic(this.offsetTopic, 2);
    createLogTopic(this.logTopic, 3);

    this.offsetRecordService = new OffsetRecordServiceImpl(new InternalOffsetRecordRepository(
        createConsumer(),
        adminClient,
        this.offsetTopic
    ));
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
      this.logTopic, this.offsetTopic
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
      this.logTopic, this.offsetTopic
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
      this.logTopic, this.offsetTopic
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
      this.logTopic, this.offsetTopic
    );

    // when then
    assertDoesNotThrow(worker::start);
  }
}
