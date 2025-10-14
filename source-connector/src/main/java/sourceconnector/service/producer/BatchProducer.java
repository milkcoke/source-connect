package sourceconnector.service.producer;

import offsetmanager.domain.OffsetRecord;
import sourceconnector.domain.batch.MessageBatch;

import java.io.Closeable;


// TODO: Compare between the AutoCloseable and Closeable
public interface BatchProducer<T>  extends Closeable {
  void sendBatch(OffsetRecord offsetRecord, MessageBatch<T> messageBatch);
}
