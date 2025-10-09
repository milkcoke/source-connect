package sourceconnector.domain.connect;

import java.util.Collection;

public interface TaskAssignor {
  /**
   * Each task should be assigned by this assignor
   * @param tasks handling the file list
   */
  void assign(Collection<Task<FileProcessingResult>> tasks);
}
