package sourceconnector.service.processor.impl;

import sourceconnector.domain.log.Log;
import sourceconnector.service.processor.BaseProcessor;

/**
 * Bypass the input with no operation
 */
public class ByPassProcessor extends BaseProcessor<Log> {
  @Override
  public Log process(Log input) {
    if(this.next == null) return input;
    return this.next.process(input);
  }
}
