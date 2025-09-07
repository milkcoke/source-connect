package sourceconnector.parser;

import sourceconnector.domain.log.FileMetadata;
import sourceconnector.domain.log.JSONLog;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.Metadata;

public class JSONLogParser implements LogParser {
  @Override
  public Log parse(String input, Metadata metadata) {
    return new JSONLog(input, (FileMetadata) metadata);
  }
}
