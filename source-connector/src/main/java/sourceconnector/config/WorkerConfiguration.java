package sourceconnector.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sourceconnector.domain.connect.FileTaskAssignor;
import sourceconnector.domain.connect.TaskAssignor;
import sourceconnector.domain.connect.Worker;
import offsetmanager.domain.file.FileKey;
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

    FileKey[] inputFileKeys = storageConfig.getAllFileKeys().toArray(new FileKey[0]);

    if (fileSearchConfigs.isRecursive()) {
      var foundFileKeys = fileLister.listFilesRecursively(inputFileKeys);
      return new FileTaskAssignor(foundFileKeys, appConfig.taskCount());
    } else {
      var foundFileKeys = fileLister.listFiles(inputFileKeys);
      return new FileTaskAssignor(foundFileKeys, appConfig.taskCount());
    }
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
