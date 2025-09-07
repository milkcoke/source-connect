package sourceconnector.service.processor.impl;

import sourceconnector.domain.log.JSONLog;
import sourceconnector.service.processor.AbstractFilterProcessor;

public class EmptyFilterProcessor extends AbstractFilterProcessor<JSONLog> {
  @Override
  protected boolean condition(JSONLog input) {
    return !input.get().isEmpty();
  }
}
