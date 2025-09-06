package sourceconnector.service.batcher;

import lombok.RequiredArgsConstructor;
import sourceconnector.domain.MessageBatch;
import sourceconnector.domain.log.FileBaseLog;
import sourceconnector.service.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class FileLogBatcher implements Batchable<FileBaseLog> {
  private final Pipeline<FileBaseLog> pipeline;
  private final int batchSize;

  @Override
  public MessageBatch<FileBaseLog> nextBatch() {
    final List<FileBaseLog> batch = new ArrayList<>(this.batchSize);

    FileBaseLog result;
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
