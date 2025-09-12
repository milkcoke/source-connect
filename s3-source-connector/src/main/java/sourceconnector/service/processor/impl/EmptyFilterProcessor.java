package sourceconnector.service.processor.impl;

import sourceconnector.domain.log.FileBaseLog;
import sourceconnector.service.processor.AbstractFilterProcessor;

public class EmptyFilterProcessor extends AbstractFilterProcessor<FileBaseLog> {
  @Override
  protected boolean condition(FileBaseLog input) {
    return !input.get().isEmpty();
  }
}
