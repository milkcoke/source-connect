package sourceconnector.config

import offsetmanager.domain.file.FileKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import sourceconnector.domain.connect.FileTaskAssignor
import sourceconnector.domain.connect.OffsetRecordService
import sourceconnector.domain.connect.TaskAssignor
import sourceconnector.domain.connect.Worker
import sourceconnector.repository.file.FileLister
import java.io.IOException

@Configuration
class WorkerConfiguration {
  @Bean
  @Throws(IOException::class)
  fun taskAssignor(
    fileLister: FileLister,
    storageConfig: StorageConfig,
    fileSearchConfigs: FileSearchConfigs,
    connectConfig: ConnectConfig,
    offsetRecordService: OffsetRecordService
  ): TaskAssignor {
    val inputFileKeys = storageConfig.allFileKeys.toTypedArray<FileKey>()

    if (fileSearchConfigs.isRecursive) {
      val foundFileKeys = fileLister.listFilesRecursively(*inputFileKeys)
      return FileTaskAssignor(foundFileKeys, connectConfig.taskCount, offsetRecordService)
    } else {
      val foundFileKeys = fileLister.listFiles(*inputFileKeys)
      return FileTaskAssignor(foundFileKeys, connectConfig.taskCount, offsetRecordService)
    }
  }

  @Bean
  fun worker(
    taskAssignor: TaskAssignor,
    @Value("\${JOB_INDEX}") jobIndex: Int
  ): Worker {
    return Worker(jobIndex, taskAssignor)
  }
}
