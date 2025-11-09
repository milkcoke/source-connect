package sourceconnector.startup;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sourceconnector.config.AppConfig;
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
public class WorkerStartupRunner implements ApplicationRunner {
  private final AppConfig appConfig;
  private final Worker worker;
  private final PipelineSupplier<Log> pipelineSupplier;
  private final Properties producerProperties;
  private final TopicConfig topicConfig;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    Collection<Task<FileProcessingResult>> tasks = worker.createTasks(
      appConfig.workerCount(), appConfig.taskCount(),
      pipelineSupplier,
      producerProperties,
      topicConfig.sinkTopic(), topicConfig.offsetTopic()
    );
    worker.start();
  }
}
