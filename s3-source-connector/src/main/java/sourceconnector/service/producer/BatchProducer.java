package sourceconnector.service.producer;

import sourceconnector.domain.batch.MessageBatch;
import sourceconnector.domain.offset.OffsetRecord;

public interface BatchProducer<T> {
  void sendBatch(OffsetRecord offsetRecord, MessageBatch<T> messageBatch);
}
