package sourceconnector.domain.connect;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import sourceconnector.repository.file.FileRepository;
import sourceconnector.service.producer.BatchProduceService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Worker is a container for running tasks.
 */
@Slf4j
public class Worker {
  private final String id;
  private final int index;
  private final TaskAssignor taskAssignor;
  private ExecutorService executor;
  private final Collection<Task<FileProcessingResult>> tasks = new ArrayList<>();

  public Worker(int index, TaskAssignor taskAssignor) {
    this.id = String.format("Worker-%d", index);
    this.index = index;
    this.taskAssignor = taskAssignor;
  }

  /**
   * Should be called only once after instantiated.
   * @param totalWorkerCount the number of total workers
   * @param totalTaskCount the number of total tasks
   * @return Tasks created in the worker
   */
  public Collection<Task<FileProcessingResult>> createTasks(
    int totalWorkerCount,
    int totalTaskCount,
    FileRepository fileRepository,
    Properties producerProperties
  ) {
    if (totalWorkerCount < 1) {
      throw new IllegalArgumentException("Total worker count should be greater than zero");
    }
    if (totalTaskCount < 1) {
      throw new IllegalArgumentException("Total task count should be greater than zero");
    }

    int quotient = totalTaskCount / totalWorkerCount;
    int remainder = totalTaskCount % totalWorkerCount;

    int startIndex = this.index * quotient + Math.min(this.index, remainder);
    int endIndex = (this.index + 1) * quotient + Math.min(this.index + 1, remainder);

    for (int taskIndex = startIndex; taskIndex < endIndex; taskIndex++) {
      producerProperties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, String.format("Task-%d-", taskIndex));

      tasks.add(new FileSourceTask(
        taskIndex,
        fileRepository,
        // FIXME: offset, log topic 명 주입받기 or DSL 을 통한 외부 설정 주입
        new BatchProduceService(producerProperties, "log-topic", "offset-topic"))
      );
    }

    this.assignTasks();
    this.executor = Executors.newFixedThreadPool(tasks.size());
    return tasks;
  }


  /**
   * Start all the tasks in this worker
   * @throws InterruptedException
   * @throws ExecutionException
   * @throws IllegalStateException trying to start before creating tasks
   */
  public void start() throws InterruptedException, ExecutionException {
    if (this.tasks.isEmpty()) {
      throw new IllegalStateException("No tasks to start");
    }

    List<Future<FileProcessingResult>> futures = this.executor.invokeAll(tasks);
    for (var future : futures) {
      FileProcessingResult result = future.get();
      log.info(
        "totalCount: {}, successCount: {}, failCount: {}",
        result.getTotalCount(),
        result.getSuccessCount(),
        result.getFailureCount()
      );
    }

    log.info("{} completed the all jobs", this.id);
  }


  private void assignTasks() {
    this.taskAssignor.assign(this.tasks);
  }
}
