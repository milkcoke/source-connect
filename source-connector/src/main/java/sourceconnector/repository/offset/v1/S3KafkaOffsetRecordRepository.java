package sourceconnector.repository.offset.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.OffsetRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Utils;
import sourceconnector.domain.offset.S3OffsetRecord;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static offsetmanager.domain.OffsetStatus.INITIAL;

@Slf4j
@RequiredArgsConstructor
public class S3KafkaOffsetRecordRepository implements KafkaOffsetRecordRepository {
  private final Consumer<String, Long> consumer;
  private final AdminClient adminClient;
  private final Duration timeout = Duration.ofMillis(100);

  @Override
  public OffsetRecord findLastOffsetRecord(String topicName, String s3key) {
    int partition = this.getPartitionsForTopic(topicName, s3key);
    TopicPartition topicPartition = new TopicPartition(topicName, partition);
    this.consumer.assign(List.of(topicPartition));
    long currentOffset = this.consumer.beginningOffsets(List.of(topicPartition)).get(topicPartition);
    // Log End Offset (LEO) for each partition which is offset appended to the topic partition
    long endOffset = this.consumer.endOffsets(List.of(topicPartition)).get(topicPartition);

    S3OffsetRecord lastOffsetRecord = new S3OffsetRecord(s3key, INITIAL.getValue());

    while (currentOffset < endOffset) {
      this.consumer.seek(topicPartition, currentOffset);

       List<ConsumerRecord<String, Long>> recordList = this.consumer
        .poll(timeout)
        .records(topicPartition);

       if (recordList.isEmpty()) break; // Should not bey empty

       long lastOffset = recordList
        .stream()
        .max(Comparator.comparingLong(ConsumerRecord::offset))
        .map(ConsumerRecord::offset)
        .get();

      currentOffset = lastOffset + 1;

      lastOffsetRecord = recordList
        .stream()
        .filter(record -> record.key().equals(s3key))
        .max(Comparator.comparingLong(ConsumerRecord::offset))
        .map(record -> new S3OffsetRecord(
          record.key(),
          record.offset()
        ))
        .orElse(lastOffsetRecord);
    }

    return lastOffsetRecord;
  }

  public int getPartitionsForTopic(String topicName, String s3Key){
    // get partition count of topic
    DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topicName));
    Map<String, KafkaFuture<TopicDescription>> futures = result.topicNameValues();
    try {
      TopicDescription description = futures.get(topicName).get();
      int partitionCount = description.partitions().size();
      return Utils.murmur2(s3Key.getBytes(StandardCharsets.UTF_8)) % partitionCount;
    } catch (ExecutionException | InterruptedException e) {
      log.error("Failed to get partitions for topic {}", topicName, e);
      throw new PartitionNotFoundException(e.getMessage());
    }
  }
}
