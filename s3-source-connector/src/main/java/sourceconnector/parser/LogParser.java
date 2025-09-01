package sourceconnector.parser;

import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.Metadata;

public interface LogParser {
  Log parse(String rawString, Metadata metadata);
}
