package sourceconnector.service.batcher

import sourceconnector.domain.batch.MessageBatch

interface Batchable<T> {
  fun hasNextBatch(): Boolean
  fun nextBatch(): MessageBatch<T>
}
