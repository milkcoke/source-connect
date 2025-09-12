package sourceconnector.service.processor.impl;

import sourceconnector.domain.log.FileBaseLog;
import sourceconnector.domain.log.JSONLog;
import sourceconnector.domain.log.Log;
import sourceconnector.service.processor.AbstractMapperProcessor;

public class TrimMapperProcessor extends AbstractMapperProcessor<FileBaseLog> {

  @Override
  protected FileBaseLog map(FileBaseLog fileBaseLog) {
    String trimmed = fileBaseLog.get().trim();
    return fileBaseLog.withPayload(trimmed);
  }
}
