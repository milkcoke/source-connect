package sourceconnector.repository.offset;

import offsetmanager.domain.OffsetRecord;

import java.util.concurrent.ExecutionException;

public interface OffsetRecordRepository {
  OffsetRecord findLastOffsetRecord(String topicName, String key);

  int getPartitionsForTopic(String topicName, String key) throws ExecutionException, InterruptedException;

  class PartitionNotFoundException extends RuntimeException {
    public PartitionNotFoundException(String message) {
      super(message);
    }
  }
}
