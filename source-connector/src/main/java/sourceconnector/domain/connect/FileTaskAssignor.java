package sourceconnector.domain.connect;

import lombok.extern.slf4j.Slf4j;
import sourceconnector.domain.file.FileKey;

import java.util.Collection;
import java.util.List;

@Slf4j
public class FileTaskAssignor implements TaskAssignor {
  private final List<FileKey> sortedAllFilePaths;
  private final int totalTaskCount;

  /**
   * All file path should be ordered since assign files to the task without duplication.
   * @param fileKeys handled by the tasks
   * @param totalTaskCount all task count
   */
  public FileTaskAssignor(List<FileKey> fileKeys, int totalTaskCount) {
    // TODO: How to be sorted FileKey?
    this.sortedAllFilePaths = fileKeys.stream()
      .sorted()
      .toList();
    this.totalTaskCount = totalTaskCount;
  }

  @Override
  public final void assign(Collection<Task<FileProcessingResult>> tasks) {
    for (var task : tasks) {
      int taskIndex = task.getIndex();

      int quotient = this.sortedAllFilePaths.size() / totalTaskCount;
      int remainder = this.sortedAllFilePaths.size() % totalTaskCount;

      int startIndex = taskIndex * quotient + Math.min(taskIndex, remainder);
      int endIndex = (taskIndex + 1) * quotient + Math.min(taskIndex + 1, remainder);

      List<FileKey> fileKeys = this.sortedAllFilePaths.subList(startIndex, endIndex);
      task.assign(fileKeys);
    }
    log.info("Completed all task assignments");
  }
}
