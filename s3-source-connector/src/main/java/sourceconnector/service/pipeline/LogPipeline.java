package sourceconnector.service.pipeline;

import sourceconnector.domain.log.Log;
import sourceconnector.service.processor.BaseProcessor;

public class LogPipeline implements Pipeline<Log> {
  private final BaseProcessor<Log> startProcessor;

  private LogPipeline(BaseProcessor<Log> startProcessor) {
    this.startProcessor = startProcessor;
  }

  @SafeVarargs
  public static Pipeline<Log> create(BaseProcessor<Log>... processors) {
    for (int i = processors.length - 1; i > 0; i--) {
      processors[i - 1].setNext(processors[i]);
    }
    return new LogPipeline(processors[0]);
  }
  @Override
  public Log getResult(Log input) {
    return this.startProcessor.process(input);
  }

}
