package sourceconnector.service.producer;

import offsetmanager.domain.OffsetRecord;
import sourceconnector.domain.batch.MessageBatch;


public interface BatchProducer<T> {
  void sendBatch(OffsetRecord offsetRecord, MessageBatch<T> messageBatch);
}
