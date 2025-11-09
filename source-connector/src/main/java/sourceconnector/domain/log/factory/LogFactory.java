package sourceconnector.domain.log.factory;

import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.LogMetadata;

public interface LogFactory {
  /**
   * @param payload log payload
   * @param logMetadata which is origin
   * @return Log
   */
  Log create(String payload, LogMetadata logMetadata);
}
