package sourceconnector.repository.offset;

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
import sourceconnector.service.offset.OffsetRecordRepository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class InternalOffsetRecordRepository implements OffsetRecordRepository {
  private final Consumer<String, Long> consumer;
  private final AdminClient adminClient;
  private final String offsetTopic;
  private final Duration timeout = Duration.ofMillis(100);

  @Override
  public Optional<OffsetRecord> findLastOffsetRecord(FileKey fileKey) {
    int partition = this.getPartitionsForTopic(fileKey);
    TopicPartition topicPartition = new TopicPartition(offsetTopic, partition);
    this.consumer.assign(List.of(topicPartition));
    long currentOffset = this.consumer.beginningOffsets(List.of(topicPartition)).get(topicPartition);
    long endOffset = this.consumer.endOffsets(List.of(topicPartition)).get(topicPartition);

    Optional<OffsetRecord> lastOffsetRecord = Optional.empty();

    while (currentOffset < endOffset) {
      this.consumer.seek(topicPartition, currentOffset);

      List<ConsumerRecord<String, Long>> recordList = this.consumer
        .poll(timeout)
        .records(topicPartition);

      if (recordList.isEmpty()) break; // Should not empty

      long lastOffset = recordList
        .stream()
        .max(Comparator.comparingLong(ConsumerRecord::offset))
        .map(ConsumerRecord::offset)
        .get();

      currentOffset = lastOffset + 1;

      Optional<OffsetRecord> currentLastOffsetRecord = recordList
        .stream()
        .filter(record -> record.key().equals(fileKey.get()))
        .max(Comparator.comparingLong(ConsumerRecord::offset))
        .map(record -> new DefaultOffsetRecord(
          FileKeyParser.parse(record.key()),
          record.value()
        ));

      if (currentLastOffsetRecord.isPresent()) {
        lastOffsetRecord = currentLastOffsetRecord;
      }
    }

    return lastOffsetRecord;
  }

  @Override
  public List<OffsetRecord> findLastOffsetRecords(List<FileKey> keys) {
    if (keys.isEmpty()) return Collections.emptyList();

    Map<Integer, List<FileKey>> keysByPartition = keys.stream()
      .collect(Collectors.groupingBy(this::getPartitionsForTopic));

    Map<FileKey, OffsetRecord> keyOffsetMap = new HashMap<>();
    // Iterate through each partition
    for (Map.Entry<Integer, List<FileKey>> entry: keysByPartition.entrySet()) {
      int partition = entry.getKey();
      Set<FileKey> fileKeySet = new HashSet<>(entry.getValue());

      TopicPartition topicPartition = new TopicPartition(offsetTopic, partition);
      this.consumer.assign(List.of(topicPartition));

      long currentOffset = this.consumer.beginningOffsets(List.of(topicPartition)).get(topicPartition);
      long endOffset = this.consumer.endOffsets(List.of(topicPartition)).get(topicPartition);

      while (currentOffset < endOffset) {
        consumer.seek(topicPartition, currentOffset);
        List<ConsumerRecord<String, Long>> recordList = this.consumer
          .poll(timeout)
          .records(topicPartition);

        if (recordList.isEmpty()) break;

        currentOffset = recordList.getLast().offset() + 1;

        for (var record: recordList) {
          FileKey fileKey = FileKeyParser.parse(record.key());
          if (!fileKeySet.contains(fileKey)) continue;
          long offset = record.value();
          // record last offset for each fileKey
          keyOffsetMap.put(fileKey, new DefaultOffsetRecord(fileKey, offset));
        }
      }

    }

    return new ArrayList<>(keyOffsetMap.values());
  }

  private int getPartitionsForTopic(FileKey fileKey){
    // get partition count of topic
    DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(offsetTopic));
    Map<String, KafkaFuture<TopicDescription>> futures = result.topicNameValues();
    try {
      TopicDescription description = futures.get(offsetTopic).get();
      int numPartitions = description.partitions().size();
      byte[] serializedKey = fileKey.get().getBytes(StandardCharsets.UTF_8);
      return Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions;
    } catch (ExecutionException | InterruptedException e) {
      log.error("Failed to get partitions for topic {}", offsetTopic, e);
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public void close() throws Exception {
    this.consumer.close();
    this.adminClient.close();
  }
}
