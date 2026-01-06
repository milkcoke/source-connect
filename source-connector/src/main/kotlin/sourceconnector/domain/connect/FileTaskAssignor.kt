package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.OffsetStatus
import org.slf4j.LoggerFactory
import kotlin.math.min

class FileTaskAssignor(
  fileKeys: List<FileKey>,
  private val totalTaskCount: Int,
  private val offsetRecordService: OffsetRecordService,
  /**
   * All file path should be ordered since assign files to the task without duplication.
   * @param fileKeys handled by the tasks
   * @param totalTaskCount all task count
   */
  private val sortedAllFilePaths: List<FileKey> = fileKeys.sorted()
) : TaskAssignor {

  private val log = LoggerFactory.getLogger(FileTaskAssignor::class.java)

  override fun assign(tasks: Collection<Task<FileProcessingResult>>) {
    val offsetMap = this.offsetRecordService.offsetMap(sortedAllFilePaths)
    for (task in tasks) {
      val taskIndex: Int = task.index

      val quotient = this.sortedAllFilePaths.size / totalTaskCount
      val remainder = this.sortedAllFilePaths.size % totalTaskCount

      val startIndex = taskIndex * quotient + min(taskIndex, remainder)
      val endIndex = (taskIndex + 1) * quotient + min(taskIndex + 1, remainder)

      val fileKeys: List<FileKey> = this.sortedAllFilePaths.subList(startIndex, endIndex)

      val fileKeyOffsetMap: Map<FileKey, Long> = fileKeys.associateWith{
        fileKey -> offsetMap.getOrDefault(fileKey, OffsetStatus.INITIAL.offset)
      }

      task.assign(fileKeyOffsetMap)
    }
    log.info("Completed all task assignments")
  }
}
