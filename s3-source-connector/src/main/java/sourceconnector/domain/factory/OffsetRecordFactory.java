package sourceconnector.domain.factory;

import sourceconnector.domain.MessageBatch;
import sourceconnector.domain.OffsetRecord;
import sourceconnector.domain.log.Log;

public interface OffsetRecordFactory<T extends Log> {
  OffsetRecord from(T log);
  OffsetRecord from(MessageBatch<T> batch);
}
