package sourceconnector.repository.offset.v1;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;

import java.util.concurrent.ExecutionException;

public interface KafkaOffsetRecordRepository {
  OffsetRecord findLastOffsetRecord(String topicName, FileKey key);

  int getPartitionsForTopic(String topicName, FileKey key) throws ExecutionException, InterruptedException;

  class PartitionNotFoundException extends RuntimeException {
    public PartitionNotFoundException(String message) {
      super(message);
    }
  }
}
