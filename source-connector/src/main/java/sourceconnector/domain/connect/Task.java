package sourceconnector.domain.connect;

import offsetmanager.domain.file.FileKey;

import java.util.Map;
import java.util.concurrent.Callable;

public interface Task<T> extends Callable<T> {
  int getIndex();

  /**
   * Assign the file paths handled by this task
   */
  void assign(Map<FileKey, Long> offsetMap);
}
