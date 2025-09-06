package sourceconnector.service.batcher;

import lombok.RequiredArgsConstructor;
import sourceconnector.domain.MessageBatch;
import sourceconnector.domain.log.Log;
import sourceconnector.service.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class LogBatcher implements Batchable<Log> {
  private final Pipeline<Log> pipeline;
  private final int batchSize;

  @Override
  public MessageBatch<Log> nextBatch() {
    final List<Log> batch = new ArrayList<>(this.batchSize);

    Log result;
    do {
      result = pipeline.getResult();
      if (result != null) batch.add(result);
    } while (
      !pipeline.isComplete() &&
      batch.size() < batchSize
    );

    if (batch.isEmpty()) return Collections::emptyList;
    return () -> batch;
  }
}
