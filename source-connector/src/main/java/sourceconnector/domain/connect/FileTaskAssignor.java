package sourceconnector.domain.connect;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

@Slf4j
public class FileTaskAssignor implements TaskAssignor {
  private final List<String> sortedAllFilePaths;
  private final int totalTaskCount;

  /**
   * All file path should be ordered since assign files to the task without duplication.
   * @param allFilePaths handled by the tasks
   * @param totalTaskCount all task count
   */
  public FileTaskAssignor(List<String> allFilePaths, int totalTaskCount) {
    this.sortedAllFilePaths = allFilePaths.stream()
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

      List<String> filePaths = this.sortedAllFilePaths.subList(startIndex, endIndex);
      task.assign(filePaths);
    }
    log.info("Completed all task assignments");
  }
}
