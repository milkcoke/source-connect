package repository;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.DefaultOffsetRecord;
import offsetmanager.domain.OffsetRecord;
import offsetmanager.manager.OffsetManager;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Should update continuously when new offsets are produced to the offset topic <br>
 * without consumer group management in the background
 */
@Slf4j
@Repository
public class RemoteOffsetManager implements OffsetManager {
  private final Map<String, OffsetRecord> offsetStore = new ConcurrentHashMap<>();
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

    boolean completeScan = endOffsets.entrySet().stream()
      .allMatch(endOffset -> consumer.position(endOffset.getKey()) >= endOffset.getValue());

    while(!completeScan) {
      ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));
      // 5-1. Process records and update offset store
      records.forEach(record ->
        this.upsert(
          record.key(),
          new DefaultOffsetRecord(record.key(), record.value())
        )
      );
      // 5-2 Only check the partitions that are not fully scanned yet
      completeScan = endOffsets.entrySet().stream()
        .allMatch(endOffset -> consumer.position(endOffset.getKey()) >= endOffset.getValue());
    }
    log.info("All partitions reached end offsets, finished the initializing.");
    this.executorService.submit(this::runUpdate);
  }

  private void runUpdate() {
    try(consumer) {
      while (true) {
        ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, Long> record : records) {
          this.upsert(record.key(), new DefaultOffsetRecord(record.key(), record.value()));
        }
      }
    } catch (Exception e) {
      log.error("Error in background offset update thread", e);
    }
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
