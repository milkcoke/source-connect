package repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.OffsetManager;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;


@Slf4j
@RequiredArgsConstructor
public class LocalOffsetManager implements OffsetManager {
  private final Map<String, Long> offsetStore = new HashMap<>();
  private final Consumer<String, Long> consumer;

  public void init() {
    List<PartitionInfo> partitionInfoList = consumer.partitionsFor("offset-topic");
    List<TopicPartition> topicPartitions = partitionInfoList.stream()
      .map(partitionInfo -> new TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
      .toList();


    // 2. Assign all partitions
    consumer.assign(topicPartitions);

    // 3. Get the end offsets
    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);

    // 4. Seek to the beginning
    consumer.seekToBeginning(topicPartitions);

    // 5. Track current offsets
    Map<TopicPartition, Long> currentOffset = new HashMap<>();
    topicPartitions.forEach(tp -> currentOffset.put(tp, consumer.position(tp)));

    Set<TopicPartition> activePartitions = new HashSet<>(topicPartitions);

    // 6. Poll loop with dynamic unassign
    while (!activePartitions.isEmpty()) {
      ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));

      for (ConsumerRecord<String, Long> record : records) {
        TopicPartition tp = new TopicPartition(record.topic(), record.partition());
        currentOffset.put(tp, record.offset() + 1);
        this.update(record.key(), record.value());

        // Reached to end offset for this partition
        if (currentOffset.get(tp) >= endOffsets.get(tp)) {
          log.info("Partition {} reached end offset {}", tp, endOffsets.get(tp));
          activePartitions.remove(tp);
          // Re-assign only the remaining partitions
          consumer.assign(activePartitions);
          for (TopicPartition remainingTp : activePartitions) {
            consumer.seek(remainingTp, currentOffset.get(remainingTp));
          }
        }
      }
    }

    log.info("All partitions reached end offsets, finished the initializing.");
  }

  @Override
  public Optional<Long> findLatestOffset(String key) {
    if (this.offsetStore.containsKey(key)) {
      return Optional.of(this.offsetStore.get(key));
    }

    return Optional.empty();
  }

  @Override
  public void update(String key, long offset) {
    this.offsetStore.put(key, offset);
  }

  @Override
  public void removeKey(String key) {
    this.offsetStore.remove(key);
  }
}
