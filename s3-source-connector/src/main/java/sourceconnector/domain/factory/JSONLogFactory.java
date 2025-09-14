package sourceconnector.domain.factory;

import sourceconnector.domain.log.JSONLog;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.LogMetadata;

public class JSONLogFactory implements LogFactory {
  @Override
  public Log create(String payload, LogMetadata logMetadata) {
    return new JSONLog(payload, logMetadata);
  }
}
