package sourceconnector.domain.connect;

import java.util.List;

public interface TaskAssignor {
  void assign(Task<FileProcessingResult> task, List<String> filePaths);
}
