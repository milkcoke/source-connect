package sourceconnector.service.batcher;

import lombok.RequiredArgsConstructor;
import sourceconnector.domain.log.FileBaseLog;
import sourceconnector.service.pipeline.Pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class FileLogBatcher implements Batchable<FileBaseLog> {
  private final Pipeline<FileBaseLog> pipeline;
  private final int batchSize;
  private final List<FileBaseLog> batch = new ArrayList<>();

  @Override
  public Collection<FileBaseLog> nextBatch() {
    FileBaseLog result;
    do {
      result = pipeline.getResult();
      this.batch.add(result);
    } while (
      result != null &&
      batch.size() < batchSize
    );
    return batch;
  }
}
