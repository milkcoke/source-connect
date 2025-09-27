package repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.DefaultOffsetRecord;
import offsetmanager.domain.OffsetRecord;
import offsetmanager.manager.OffsetManager;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
public class LocalOffsetManager implements OffsetManager {
  private final Map<String, OffsetRecord> offsetStore = new HashMap<>();
  private final String offsetTopic;

  /**
  * Scan all partitions of the offset topic to build the offset store. <br>
  * This method will block until all partitions are scanned to their end offsets. <br>
  * After this method is called, the offset store is ready to be used.
  */
  public LocalOffsetManager(Consumer<String, Long> consumer, String offsetTopic) {
    this.offsetTopic = offsetTopic;
    List<PartitionInfo> partitionInfoList = consumer.partitionsFor(offsetTopic);
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
    Set<TopicPartition> activePartitions = topicPartitions.stream()
            .filter(tp -> nextOffset.get(tp) < endOffsets.get(tp))
            .collect(Collectors.toSet());

    // 6. Poll loop with dynamic unassign
    while (!activePartitions.isEmpty()) {
      // last offset is updated automatically whenever poll is called
      // last offset is from FetchResponse by TopicPartition
      ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));

      // 6-1. Process records and update offset store
      for (ConsumerRecord<String, Long> record : records) {
        this.upsert(record.key(), new DefaultOffsetRecord(record.key(), record.value()));
      }

      // 6-2. Update nextOffset for all active partitions and Check for partitions that reached end offset
      Set<TopicPartition> copyPartitionSet = new HashSet<>(activePartitions);
      for (TopicPartition tp : copyPartitionSet) {
        long currentLastOffset = consumer.position(tp);
        nextOffset.put(tp, currentLastOffset);

        if (currentLastOffset >= endOffsets.get(tp)) {
          log.info("Partition {} reached end offset {}", tp, endOffsets.get(tp));
          activePartitions.remove(tp);
        }
      }

      // 6-3 Re-assign only when active partition set is changed
      if (activePartitions.size() != consumer.assignment().size()) {
        consumer.assign(activePartitions);
      }
    }

    consumer.close();
    log.info("All partitions reached end offsets, finished the initializing.");
}

  @Override
  public Optional<OffsetRecord> findLatestOffsetRecord(String key) {
    if (this.offsetStore.containsKey(key)) {
      return Optional.of(this.offsetStore.get(key));
    }

    return Optional.empty();
  }

  @Override
  public List<OffsetRecord> findLatestOffsetRecords(List<String> keys) {
    return keys.stream()
      .map(this.offsetStore::get)
      .filter(Objects::nonNull)
      .toList();
  }


  @Override
  public void upsert(String key, OffsetRecord offsetRecord) {
    this.offsetStore.put(key, offsetRecord);
  }

  @Override
  public void removeKey(String key) {
    this.offsetStore.remove(key);
  }

}
