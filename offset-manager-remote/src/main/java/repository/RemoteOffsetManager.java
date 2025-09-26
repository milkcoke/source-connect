package repository;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.OffsetManager;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Should update continuously when new offsets are produced to the offset topic.
 * without consumer group management in the background
 */
@Slf4j
public class RemoteOffsetManager implements OffsetManager {
  private final Map<String, Long> offsetStore = new ConcurrentHashMap<>();
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final Consumer<String, Long> consumer;
  private final String offsetTopic;

  public RemoteOffsetManager(Consumer<String, Long> consumer, String offsetTopic) {
    this.consumer = consumer;
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
    Map<TopicPartition, Long> earliestOffsets = consumer.beginningOffsets(topicPartitions);

    // Initialize scanned partitions which are already scanned to the end
    Set<TopicPartition> scannedPartitions = topicPartitions.stream()
      .filter(tp -> earliestOffsets.get(tp) >= endOffsets.get(tp))
      .collect(Collectors.toSet());

    // 6. Scan all partitions until all partitions reach end offsets
    while (topicPartitions.size() != scannedPartitions.size()) {
      ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));

      // 6-1. Process records and update offset store
      for (ConsumerRecord<String, Long> record : records) {
        this.update(record.key(), record.value());
      }

      // 6-2 Only check the partitions that are not fully scanned yet
      // FIXME: If Transaction markers exists between earliestOffsets and endOffsets, it could not be detected.
      Set<TopicPartition> retrievedPartitions = records.partitions();
      for (TopicPartition tp : retrievedPartitions) {
        long currentLastOffset = consumer.position(tp);

          // 6-3. Pause the fetch about the partitions that reached end offsets
        if (currentLastOffset >= endOffsets.get(tp)) {
          log.info("Partition {} reached end offset {}", tp, endOffsets.get(tp));
          consumer.pause(Collections.singleton(tp));
          scannedPartitions.add(tp);
        }
      }
    }

      // TODO: Refactor this
      //    (1) pause and resume approach << pause protects from sending fetch request to the partition
      //     (2) Remove a partition from assignment and Reassign the all partitions at the end.

    consumer.resume(scannedPartitions);
    // resume all partitions
    log.info("All partitions reached end offsets, finished the initializing.");
    this.executorService.submit(this::runUpdate);
  }

  private void runUpdate() {
    try(consumer) {
      while (true) {
        ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, Long> record : records) {
          this.update(record.key(), record.value());
        }
      }
    } catch (Exception e) {
      log.error("Error in background offset update thread", e);
    }
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

  @PreDestroy
  public void shutdown() {
    log.info("Shutting down RemoteOffsetManager...");
    this.executorService.shutdownNow(); // interrupts the thread running pollLoop
    try {
      if (!this.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        log.warn("Executor did not terminate cleanly");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
