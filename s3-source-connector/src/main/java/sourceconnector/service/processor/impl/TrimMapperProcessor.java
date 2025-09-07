package sourceconnector.service.processor.impl;

import sourceconnector.domain.log.JSONLog;
import sourceconnector.service.processor.AbstractMapperProcessor;

public class TrimMapperProcessor extends AbstractMapperProcessor<JSONLog> {

  @Override
  protected JSONLog map(JSONLog jsonLog) {
    String trimmed = jsonLog.get().trim();
    return jsonLog.withPayload(trimmed);
  }
}
