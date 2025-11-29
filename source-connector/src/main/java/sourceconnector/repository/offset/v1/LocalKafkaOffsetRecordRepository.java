package sourceconnector.repository.offset.v1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static offsetmanager.domain.offset.OffsetStatus.INITIAL;

@Slf4j
@RequiredArgsConstructor
public class LocalKafkaOffsetRecordRepository implements KafkaOffsetRecordRepository {
  private final Consumer<String, Long> consumer;
  private final AdminClient adminClient;
  private final Duration timeout = Duration.ofMillis(100);

  @Override
  public OffsetRecord findLastOffsetRecord(String topicName, FileKey fileKey) {
    int partition = this.getPartitionsForTopic(topicName, fileKey);
    TopicPartition topicPartition = new TopicPartition(topicName, partition);
    this.consumer.assign(List.of(topicPartition));
    long currentOffset = this.consumer.beginningOffsets(List.of(topicPartition)).get(topicPartition);
    long endOffset = this.consumer.endOffsets(List.of(topicPartition)).get(topicPartition);

    DefaultOffsetRecord lastOffsetRecord = new DefaultOffsetRecord(fileKey, INITIAL.getValue());

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
        .filter(record -> record.key().equals(fileKey.get()))
        .max(Comparator.comparingLong(ConsumerRecord::offset))
        .map(record -> new DefaultOffsetRecord(
          FileKeyParser.parse(record.key()),
          record.value()
        ))
        .orElse(lastOffsetRecord);
    }

    return lastOffsetRecord;
  }

  public int getPartitionsForTopic(String topicName, FileKey fileKey){
    // get partition count of topic
    DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topicName));
    Map<String, KafkaFuture<TopicDescription>> futures = result.topicNameValues();
    try {
      TopicDescription description = futures.get(topicName).get();
      int partitionCount = description.partitions().size();
      return Utils.murmur2(fileKey.get().getBytes(StandardCharsets.UTF_8)) % partitionCount;
    } catch (ExecutionException | InterruptedException e) {
      log.error("Failed to get partitions for topic {}", topicName, e);
      throw new PartitionNotFoundException(e.getMessage());
    }
  }
}
