package sourceconnector.repository;

import sourceconnector.domain.offset.OffsetRecord;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface OffsetRepository {
  OffsetRecord getLastOffsetRecord(String topicName, String key);
  List<OffsetRecord> findOffsetRecords(String topicName, String key);

  int getPartitionsForTopic(String topicName, String s3Key) throws ExecutionException, InterruptedException;

  class PartitionNotFoundException extends RuntimeException {
    public PartitionNotFoundException(String message) {
      super(message);
    }
  }
}
