package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey
import java.util.concurrent.Callable

interface Task<T> : Callable<T> {
  val index: Int

  /**
   * Assign the file paths handled by this task
   */
  fun assign(offsetMap: Map<FileKey, Long>)
}
