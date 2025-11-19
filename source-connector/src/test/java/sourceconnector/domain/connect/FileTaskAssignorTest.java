package sourceconnector.domain.connect;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import offsetmanager.domain.file.FileKey;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder;
import sourceconnector.domain.pipeline.factory.FileLogPipelineSupplier;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.service.producer.BatchProduceService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileTaskAssignorTest {
  private final Properties producerProperties = new Properties();
  private final PipelineSupplier<Log> pipelineSupplier = new FileLogPipelineSupplier(
    new FileBaseLogPipelineBuilder(),
    new LocalFileRepository(),
    new JSONLogFactory(),
    Collections::emptyList
  );

  @BeforeAll
  void setUp() {
    producerProperties.putAll(Map.of(
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class,
      ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-task-"
    ));
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
    TaskAssignor taskAssignor = new FileTaskAssignor(fileKeys, 5);
    FileSourceTask task0 = new FileSourceTask(0, pipelineSupplier, new BatchProduceService(producerProperties, "offset-topic", "log-topic"));

    // when
    taskAssignor.assign(List.of(task0));

    // then
    assertThat(task0.getFileKeys())
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
    TaskAssignor taskAssignor = new FileTaskAssignor(fileKeys, 4);
    FileSourceTask task0 = new FileSourceTask(0, pipelineSupplier, new BatchProduceService(producerProperties, "offset-topic", "log-topic"));
    FileSourceTask task1 =  new FileSourceTask(1, pipelineSupplier, new BatchProduceService(producerProperties, "offset-topic", "log-topic"));
    FileSourceTask task2 =  new FileSourceTask(2, pipelineSupplier, new BatchProduceService(producerProperties, "offset-topic", "log-topic"));
    FileSourceTask task3 =   new FileSourceTask(3, pipelineSupplier, new BatchProduceService(producerProperties, "offset-topic", "log-topic"));

    Collection<Task<FileProcessingResult>> tasks = List.of(task0, task1, task2, task3);

    // when
    taskAssignor.assign(tasks);

    // then
    assertAll(
      ()->assertThat(task0.getFileKeys()).hasSize(8),
      ()->assertThat(task1.getFileKeys()).hasSize(8),
      ()->assertThat(task2.getFileKeys()).hasSize(7),
      ()->assertThat(task3.getFileKeys()).hasSize(7)
    );
  }

  @DisplayName("Can assign even though file path has no element")
  @Test
  void emptyFilePathAssignTest() {
    // given
    TaskAssignor taskAssignor = new FileTaskAssignor(Collections.emptyList(), 1);
    Collection<Task<FileProcessingResult>> tasks = List.of(
      new FileSourceTask( 0, pipelineSupplier, new BatchProduceService(producerProperties, "offset-topic", "log-topic"))
    );

    // when then
    assertDoesNotThrow(() -> taskAssignor.assign(tasks));
  }

}
