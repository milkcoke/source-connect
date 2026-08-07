package sourceconnector.service.producer

import offsetmanager.domain.offset.OffsetRecord
import sourceconnector.domain.batch.MessageBatch
import java.lang.AutoCloseable

interface BatchProducer<T> : AutoCloseable {
  fun sendBatch(
    offsetRecord: OffsetRecord,
    messageBatch: MessageBatch<T>
  ): BatchResult
}
