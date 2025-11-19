package sourceconnector.service.producer;

import offsetmanager.domain.offset.OffsetRecord;
import sourceconnector.domain.batch.MessageBatch;


public interface BatchProducer<T>  extends AutoCloseable {
  void sendBatch(OffsetRecord offsetRecord, MessageBatch<T> messageBatch);
}
