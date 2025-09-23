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
  private final String offsetTopic;
  private boolean initialized = false;

  public void init() {
    List<PartitionInfo> partitionInfoList = consumer.partitionsFor(this.offsetTopic);
    List<TopicPartition> topicPartitions = partitionInfoList.stream()
      .map(partitionInfo -> new TopicPartition(partitionInfo.topic(), partitionInfo.partition()))
      .toList();


    // 2. Assign all partitions
    consumer.assign(topicPartitions);

    // 3. Get the end offsets
    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);

    // 4. Seek to the beginning
    consumer.seekToBeginning(topicPartitions);

    // 5. Track next offsets to read
    Map<TopicPartition, Long> nextOffset = consumer.beginningOffsets(topicPartitions);

    // If beginning offset >= end offset, no need to poll
    topicPartitions = topicPartitions.stream()
        .filter(tp-> nextOffset.get(tp) < endOffsets.get(tp))
        .toList();

    Set<TopicPartition> activePartitions = new HashSet<>(topicPartitions);

    // 6. Poll loop with dynamic unassign
    while (!activePartitions.isEmpty()) {
      // last offset is updated automatically whenever poll is called
      // last offset is from FetchResponse by TopicPartition
      ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));

      Set<TopicPartition> scannedPartitions = new HashSet<>(activePartitions);
      for (ConsumerRecord<String, Long> record : records) {
        TopicPartition tp = new TopicPartition(record.topic(), record.partition());
        scannedPartitions.add(tp);
        this.update(record.key(), record.value());
      }

      for (TopicPartition tp : scannedPartitions) {
        long lastOffset = consumer.position(tp);
        nextOffset.put(tp, lastOffset);
      }


      for (TopicPartition tp : scannedPartitions) {
        // Reached to end offset for this partition
        if (nextOffset.get(tp) >= endOffsets.get(tp)) {
          log.info("Partition {} reached end offset {}", tp, endOffsets.get(tp));
          activePartitions.remove(tp);
        }
      }

      // Re-assign only the remaining partitions
      consumer.assign(activePartitions);
      for (TopicPartition remainingTp : activePartitions) {
        consumer.seek(remainingTp, nextOffset.get(remainingTp));
        log.info("Reassigned partition {} to offset {}", remainingTp, nextOffset.get(remainingTp));
      }

    }

    this.initialized = true;
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

  @Override
  public boolean isInitialized() {
    return this.initialized;
  }
}
