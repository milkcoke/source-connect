package sourceconnector.domain.connect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.LocalFileRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FileTaskAssignorTest {

  @DisplayName("First task has the number of 6 file paths when task 5 and file paths 30 provided")
  @Test
  void assignedFileCountTest() {

    // given
    List<String> filePaths = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      filePaths.add("file-" + i);
    }
    TaskAssignor taskAssignor = new FileTaskAssignor(filePaths, 5);
    FileSourceTask task0 = new FileSourceTask(0, new LocalFileRepository());

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
    FileSourceTask task0 = new FileSourceTask(0, new LocalFileRepository());
    FileSourceTask task1 =  new FileSourceTask(1, new LocalFileRepository());
    FileSourceTask task2 =  new FileSourceTask(2, new LocalFileRepository());
    FileSourceTask task3 =   new FileSourceTask(3, new LocalFileRepository());

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
      new FileSourceTask( 0, new LocalFileRepository())
    );

    // when then
    assertDoesNotThrow(() -> taskAssignor.assign(tasks));
  }

}
