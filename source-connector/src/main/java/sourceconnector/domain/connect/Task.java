package sourceconnector.domain.connect;

import sourceconnector.domain.file.FileKey;

import java.util.List;
import java.util.concurrent.Callable;

public interface Task<T> extends Callable<T> {
  int getIndex();

  /**
   * Assign the file paths handled by this task
   */
  void assign(List<FileKey> fileKeys);
}
