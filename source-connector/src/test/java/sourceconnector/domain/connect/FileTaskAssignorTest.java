package sourceconnector.domain.connect;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.file.FileKey;
import org.junit.jupiter.api.*;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder;
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.repository.offset.InternalOffsetRecordRepository;
import sourceconnector.service.offset.OffsetRecordServiceImpl;
import sourceconnector.service.producer.BatchProduceService;
import sourceconnector.support.KafkaTestSupport;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileTaskAssignorTest extends KafkaTestSupport {
  private final PipelineSupplier<Log> pipelineSupplier = new FileLogPipelineSupplier(
    new FileBaseLogPipelineBuilder(),
    new LocalFileRepository(),
    new JSONLogFactory(),
    Collections::emptyList
  );
  private OffsetRecordService offsetRecordService;

  private final String offsetTopic = "test-offset";
  private final String logTopic = "test-log";

  @BeforeAll
  void setUp() {
    createOffsetTopic(this.offsetTopic, 3);
    createLogTopic(this.logTopic, 3);
    this.offsetRecordService = new OffsetRecordServiceImpl(
      new InternalOffsetRecordRepository(
      createConsumer(),
      adminClient,
      this.offsetTopic)
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
      new BatchProduceService(producerProperties, this.offsetTopic, this.logTopic));

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
    FileSourceTask task0 = new FileSourceTask(0, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic, this.offsetTopic));
    FileSourceTask task1 =  new FileSourceTask(1, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic, this.offsetTopic));
    FileSourceTask task2 =  new FileSourceTask(2, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic, this.offsetTopic));
    FileSourceTask task3 =   new FileSourceTask(3, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic, this.offsetTopic));

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
      new FileSourceTask( 0, pipelineSupplier, new BatchProduceService(producerProperties, this.logTopic, this.offsetTopic))
    );

    // when then
    assertDoesNotThrow(() -> taskAssignor.assign(tasks));
  }

}
