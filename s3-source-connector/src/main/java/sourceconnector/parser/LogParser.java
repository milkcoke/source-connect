package sourceconnector.parser;

import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.LogMetadata;

public interface LogParser {
  Log parse(String rawString, LogMetadata logMetadata);
}
