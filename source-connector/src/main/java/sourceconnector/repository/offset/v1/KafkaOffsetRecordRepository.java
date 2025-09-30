package sourceconnector.repository.offset.v1;

import offsetmanager.domain.OffsetRecord;

import java.util.concurrent.ExecutionException;

public interface KafkaOffsetRecordRepository {
  OffsetRecord findLastOffsetRecord(String topicName, String key);

  int getPartitionsForTopic(String topicName, String key) throws ExecutionException, InterruptedException;

  class PartitionNotFoundException extends RuntimeException {
    public PartitionNotFoundException(String message) {
      super(message);
    }
  }
}
