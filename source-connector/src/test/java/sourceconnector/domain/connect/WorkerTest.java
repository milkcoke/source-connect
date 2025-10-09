package sourceconnector.domain.connect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.LocalFileRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class WorkerTest {

  @DisplayName("Should throw IllegalArgumentException when worker count is 0")
  @Test
  void createNoWorkerTest() {
    // given
    Worker worker = new Worker(0, new FileTaskAssignor(Collections.emptyList(), 0));

    // when then
    assertThatThrownBy(() -> worker.createTasks(0, 1, new LocalFileRepository()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Total worker count should be greater than zero");
  }


  @DisplayName("Should throw IllegalArgumentException when task count is 0")
  @Test
  void createNoTasksTest() {
    // given
    Worker worker = new Worker(0, new FileTaskAssignor(Collections.emptyList(), 0));

    // when then
    assertThatThrownBy(() -> worker.createTasks(1, 0, new LocalFileRepository()))
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
    Collection<Task<FileProcessingResult>> tasks = worker.createTasks(1, 2, new LocalFileRepository());

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
    worker.createTasks(1, 2, new LocalFileRepository());

    // when then
    assertDoesNotThrow(worker::start);
  }
}
