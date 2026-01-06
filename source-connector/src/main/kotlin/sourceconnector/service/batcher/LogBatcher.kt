package sourceconnector.service.batcher

import sourceconnector.domain.batch.MessageBatch
import sourceconnector.domain.log.Log
import sourceconnector.domain.pipeline.Pipeline

class LogBatcher(
  private val pipeline: Pipeline<Log?>,
  private val batchSize: Int
) : Batchable<Log> {

  override fun hasNextBatch(): Boolean {
    return !pipeline.isComplete
  }

  override fun nextBatch(): MessageBatch<Log> {
    val batch: MutableList<Log> = ArrayList(this.batchSize)

    var result: Log?
    do {
      result = pipeline.getResult()
      if (result != null) batch.add(result)
    } while (!pipeline.isComplete &&
      batch.size < batchSize
    )

    if (batch.isEmpty()) return MessageBatch { mutableListOf() }
    return MessageBatch { batch }
  }
}
