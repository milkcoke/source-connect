package sourceconnector.service.processor.impl;

import sourceconnector.domain.log.Log;
import sourceconnector.service.processor.AbstractFilterProcessor;

public class EmptyFilterProcessor extends AbstractFilterProcessor<Log> {
  @Override
  protected boolean condition(Log input) {
    return !input.get().isEmpty();
  }
}
