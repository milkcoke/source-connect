package sourceconnector.service.batcher;

import sourceconnector.domain.batch.MessageBatch;

public interface Batchable<T> {
  MessageBatch<T> nextBatch();
}
