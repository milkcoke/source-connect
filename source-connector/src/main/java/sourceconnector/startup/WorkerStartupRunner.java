package sourceconnector.startup;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sourceconnector.config.AppConfig;
import sourceconnector.config.TopicConfig;
import sourceconnector.domain.connect.Worker;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class WorkerStartupRunner implements ApplicationRunner {
  private final Worker worker;
  private final AppConfig appConfig;
  private final PipelineSupplier<Log> pipelineSupplier;
  private final Properties producerProperties;
  private final TopicConfig topicConfig;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    worker.createTasks(
      appConfig.workerCount(), appConfig.taskCount(),
      pipelineSupplier,
      producerProperties,
      topicConfig.offsetTopic(), topicConfig.sinkTopic()
    );

    worker.start();
  }
}
