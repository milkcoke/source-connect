package sourceconnector.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sourceconnector.config.ConnectConfig;
import sourceconnector.config.TopicConfig;
import sourceconnector.domain.connect.FileProcessingResult;
import sourceconnector.domain.connect.Task;
import sourceconnector.domain.connect.Worker;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;

import java.util.Collection;
import java.util.Properties;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkerStartupRunner implements ApplicationRunner {
  private final ConnectConfig appConfig;
  private final Worker worker;
  private final PipelineSupplier<Log> pipelineSupplier;
  private final Properties producerProperties;
  private final TopicConfig topicConfig;
  private final ApplicationContext applicationContext;

  @Override
  public void run(ApplicationArguments args) {
    int exitCode = 0;
    try {
      Collection<Task<FileProcessingResult>> tasks = worker.createTasks(
        appConfig.workerCount(), appConfig.taskCount(),
        pipelineSupplier,
        producerProperties,
        topicConfig.sinkTopic(), topicConfig.offsetTopic()
      );
      worker.start();
    } catch (Exception e) {
      exitCode = 1;
      log.error("Error occurred during worker execution", e);
    } finally {
      int finalExitCode = exitCode;
      SpringApplication.exit(applicationContext, () -> finalExitCode);
    }
    log.info("Completed all tasks, shutting down application.");
    System.exit(exitCode);
  }
}
