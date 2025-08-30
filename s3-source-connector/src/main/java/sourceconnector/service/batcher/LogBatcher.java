package sourceconnector.service.batcher;

import lombok.RequiredArgsConstructor;
import sourceconnector.domain.log.Log;
import sourceconnector.service.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class LogBatcher implements Batcherable<Log> {
  private final Pipeline<Log> pipeline;
  private final int batchSize;
  private final List<Log> batch = new ArrayList<>();
  private int currentSize;
  @Override
  public Collection<Log> getBatch() {
    while(currentSize < batchSize) {
//      pipeline.getResult()
//      batch.add()
    }
  }
}
