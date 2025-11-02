package sourceconnector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sourceconnector.domain.connect.FileTaskAssignor;
import sourceconnector.domain.connect.TaskAssignor;
import sourceconnector.domain.connect.Worker;
import sourceconnector.repository.file.FileLister;

import java.io.IOException;

@Configuration
public class WorkerConfiguration {

  @Bean
  public TaskAssignor taskAssignor(
    FileLister fileLister,
    StorageConfig storageConfig,
    FileSearchConfigs fileSearchConfigs,
    AppConfig appConfig
  ) throws IOException {

    var allFilePaths = fileLister.listFiles(
      fileSearchConfigs.isRecursive(),
      storageConfig.paths().toArray(String[]::new)
    );

    return new FileTaskAssignor(allFilePaths, appConfig.taskCount());
  }

  @Bean
  public Worker worker(
    TaskAssignor taskAssignor,
    @Value("${JOB_INDEX}")
    int jobIndex
  ) {
    return new Worker(jobIndex, taskAssignor);
  }
}
