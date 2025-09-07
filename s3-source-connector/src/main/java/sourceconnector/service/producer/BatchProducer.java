package sourceconnector.service.producer;

import sourceconnector.domain.MessageBatch;
import sourceconnector.domain.OffsetRecord;

public interface BatchProducer<T> {
  void sendBatch(OffsetRecord offsetRecord, MessageBatch<T> messageBatch);
}
