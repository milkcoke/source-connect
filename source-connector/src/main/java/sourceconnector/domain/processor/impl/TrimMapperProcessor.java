package sourceconnector.domain.processor.impl;

import lombok.RequiredArgsConstructor;
import sourceconnector.domain.log.factory.LogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.processor.AbstractMapperProcessor;

/**
 * Remove the whitespace in the Log
 */
@RequiredArgsConstructor
public class TrimMapperProcessor extends AbstractMapperProcessor<Log> {
  private final LogFactory logFactory;

  @Override
  protected Log map(Log log) {
    String trimmed = log.get().trim();
    return logFactory.create(trimmed, log.getMetadata());
  }
}
