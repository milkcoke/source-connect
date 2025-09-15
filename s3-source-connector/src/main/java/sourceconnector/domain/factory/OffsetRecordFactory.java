package sourceconnector.domain.factory;

import sourceconnector.domain.batch.MessageBatch;
import sourceconnector.domain.offset.OffsetRecord;
import sourceconnector.domain.log.Log;

public interface OffsetRecordFactory<T extends Log> {
  OffsetRecord from(T log);
  OffsetRecord from(MessageBatch<T> batch);
}
