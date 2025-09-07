package sourceconnector.service.batcher;

import sourceconnector.domain.MessageBatch;

public interface Batchable<T> {
  MessageBatch<T> nextBatch();
}
