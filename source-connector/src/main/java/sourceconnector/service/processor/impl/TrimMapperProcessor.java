package sourceconnector.service.processor.impl;

import lombok.RequiredArgsConstructor;
import sourceconnector.domain.factory.LogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.service.processor.AbstractMapperProcessor;

@RequiredArgsConstructor
public class TrimMapperProcessor extends AbstractMapperProcessor<Log> {
  private final LogFactory logFactory;

  @Override
  protected Log map(Log log) {
    String trimmed = log.get().trim();
    return logFactory.create(trimmed, log.getMetadata());
  }
}
