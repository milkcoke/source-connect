package sourceconnector.domain.log.factory

import sourceconnector.domain.log.JSONLog
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.LogMetadata

class JSONLogFactory : LogFactory {
  override fun create(payload: String, logMetadata: LogMetadata): Log {
    return JSONLog(payload, logMetadata)
  }
}
