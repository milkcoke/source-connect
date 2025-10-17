package sourceconnector.service.producer;

import offsetmanager.domain.OffsetRecord;
import sourceconnector.domain.batch.MessageBatch;

import java.io.Closeable;


public interface BatchProducer<T>  extends AutoCloseable {
  void sendBatch(OffsetRecord offsetRecord, MessageBatch<T> messageBatch);
}
