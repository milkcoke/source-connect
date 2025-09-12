package sourceconnector.domain.factory;

import sourceconnector.domain.log.*;

public class FileBaseLogFactory {

  /**
   * Create FileBaseLog
   * @param payload log payload
   * @param logMetadata log metadata which is origin
   * @return FileBaseLog
   */
  public FileBaseLog create(String payload, FileLogMetadata logMetadata) {
    return new JSONLog(payload, logMetadata);
  }
}
