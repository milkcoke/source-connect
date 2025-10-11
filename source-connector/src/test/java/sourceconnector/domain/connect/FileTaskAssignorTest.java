package sourceconnector.domain.connect;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.service.producer.BatchProduceService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileTaskAssignorTest {
  private final Properties producerProperties = new Properties();

  @BeforeAll
  void setUp() {
    producerProperties.putAll(Map.of(
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class,
      ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-task-"
    ));
  }

  @DisplayName("First task has the number of 6 file paths when task 5 and file paths 30 provided")
  @Test
  void assignedFileCountTest() {

    // given
    List<String> filePaths = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      filePaths.add("file-" + i);
    }
    TaskAssignor taskAssignor = new FileTaskAssignor(filePaths, 5);
    FileSourceTask task0 = new FileSourceTask(0, new LocalFileRepository(), new BatchProduceService(producerProperties, "offset-topic", "log-topic"));

    // when
    taskAssignor.assign(List.of(task0));

    // then
    assertThat(task0.getFilePaths())
      .hasSize(6);
  }

  @DisplayName("First and second task have the number of 8 file paths when task 4 and file paths 30 provided")
  @Test
  void assignedFileCountTest2() {
    // given
    List<String> filePaths = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      filePaths.add("file-" + i);
    }
    TaskAssignor taskAssignor = new FileTaskAssignor(filePaths, 4);
    FileSourceTask task0 = new FileSourceTask(0, new LocalFileRepository(), new BatchProduceService(producerProperties, "offset-topic", "log-topic"));
    FileSourceTask task1 =  new FileSourceTask(1, new LocalFileRepository(), new BatchProduceService(producerProperties, "offset-topic", "log-topic"));
    FileSourceTask task2 =  new FileSourceTask(2, new LocalFileRepository(), new BatchProduceService(producerProperties, "offset-topic", "log-topic"));
    FileSourceTask task3 =   new FileSourceTask(3, new LocalFileRepository(), new BatchProduceService(producerProperties, "offset-topic", "log-topic"));

    Collection<Task<FileProcessingResult>> tasks = List.of(task0, task1, task2, task3);

    // when
    taskAssignor.assign(tasks);

    // then
    assertAll(
      ()->assertThat(task0.getFilePaths()).hasSize(8),
      ()->assertThat(task1.getFilePaths()).hasSize(8),
      ()->assertThat(task2.getFilePaths()).hasSize(7),
      ()->assertThat(task3.getFilePaths()).hasSize(7)
    );
  }

  @DisplayName("Can assign even though file path has no element")
  @Test
  void emptyFilePathAssignTest() {
    // given
    TaskAssignor taskAssignor = new FileTaskAssignor(Collections.emptyList(), 1);
    Collection<Task<FileProcessingResult>> tasks = List.of(
      new FileSourceTask( 0, new LocalFileRepository(), new BatchProduceService(producerProperties, "offset-topic", "log-topic"))
    );

    // when then
    assertDoesNotThrow(() -> taskAssignor.assign(tasks));
  }

}
