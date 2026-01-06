package sourceconnector.domain.log.factory

import sourceconnector.domain.log.Log
import sourceconnector.domain.log.LogMetadata

interface LogFactory {
  /**
   * @param payload log payload
   * @param logMetadata which is origin
   * @return Log
   */
  fun create(payload: String, logMetadata: LogMetadata): Log
}
