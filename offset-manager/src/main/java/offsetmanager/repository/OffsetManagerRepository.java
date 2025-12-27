package offsetmanager.repository;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.OffsetStorage;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetRecord;
import offsetmanager.manager.OffsetManager;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.kafka.common.utils.Utils.sleep;

/**
 * Should update continuously when new offsets are produced to the offset topic <br>
 * without consumer group management in the background
 */
@Slf4j
@Repository
public class OffsetManagerRepository implements OffsetManager {
  private final OffsetStorage offsetStorage;
  private final String offsetTopicName;
  private final Properties consumerProperties;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final AtomicReference<Consumer<String, Long>> activeConsumer = new AtomicReference<>();
  /**
   * indicates whether the background consumer loop is running
   * This is used for shutdown and restart logic
   */
  private final AtomicBoolean isRunning = new AtomicBoolean(true);
  private final AtomicBoolean isReady = new AtomicBoolean(false);

  public OffsetManagerRepository(
    OffsetStorage offsetStorage,
    Properties consumerProperties,
    String offsetTopicName
  ) {
    this.offsetStorage = offsetStorage;
    this.consumerProperties = consumerProperties;
    this.offsetTopicName = offsetTopicName;
    this.executorService.submit(this::run);
  }


  private void run() {
    while (isRunning.get()) {

      // reset readiness BEFORE starting a new consumer
      isReady.set(false);
      isRunning.set(false);
      this.activeConsumer.set(null);
      this.offsetStorage.clear();

      try (Consumer<String, Long> consumer = new KafkaConsumer<>(consumerProperties)) {

        log.info("Starting OffsetManager consumer");

        List<TopicPartition> partitions = discoverPartitions(consumer, offsetTopicName);
        this.activeConsumer.set(consumer);

        initializeSnapShot(consumer, partitions);

        // publish readiness
        isRunning.set(true);
        isReady.set(true);
        log.info("OffsetManager is ready state");

        // blocks until exception or shutdown
        streamUpdates(consumer);

      } catch (WakeupException e) {
        if (!isRunning.get()) {
          log.info("OffsetManager shutting down");
          return;
        }
        log.warn("WakeupException during run loop", e);

      } catch (Exception e) {
        // RECOVERABLE failure
        log.warn("Consumer failed", e);

        // readiness already reset at loop top
        sleep(3000); // backoff before retry

      }
    }
  }

  private List<TopicPartition> discoverPartitions(Consumer<?, ?> consumer, String topic) {
    List<PartitionInfo> infos = consumer.partitionsFor(topic);

    if (infos == null || infos.isEmpty()) {
      throw new IllegalStateException("Topic not available: " + topic);
    }

    return infos.stream()
      .map(p -> new TopicPartition(p.topic(), p.partition()))
      .toList();
  }

  private void initializeSnapShot(Consumer<String, Long> consumer, List<TopicPartition> topicPartitions) {

    // 1. Assign all partitions
    consumer.assign(topicPartitions);

    // 2. Barrier step for initializing fetch session to complete, Checking topic partition is ready
    consumer.poll(Duration.ZERO);

    // 3. Get the end offsets
    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);

    // 4. Seek to the beginning
    consumer.seekToBeginning(topicPartitions);

    // 5. Processing all records until reaching end offsets
    Set<TopicPartition> remaining = new HashSet<>(topicPartitions);

    while (!remaining.isEmpty()) {
      ConsumerRecords<String, Long> records =
        consumer.poll(Duration.ofMillis(100));

      for (ConsumerRecord<String, Long> record : records) {
        FileKey fileKey = FileKeyParser.parse(record.key());
        upsert(fileKey, new DefaultOffsetRecord(fileKey, record.value()));
      }

      // Check progress only on unfinished partitions
      for (Iterator<TopicPartition> it = remaining.iterator(); it.hasNext();) {
        TopicPartition tp = it.next();
        long currentOffset = consumer.position(tp);
        long endOffset = endOffsets.get(tp);

        if (currentOffset >= endOffset) {
          it.remove();
        }
      }
    }

    log.info("Snapshot initialization completed for all partitions");
  }

  private void streamUpdates(Consumer<String, Long> consumer) {
    while (isRunning.get()) {
      ConsumerRecords<String, Long> records =
        consumer.poll(Duration.ofMillis(100));

      for (ConsumerRecord<String, Long> record : records) {
        FileKey key = FileKeyParser.parse(record.key());
        if (record.value() == null) {
          this.removeKey(key);
        } else {
          this.upsert(key, new DefaultOffsetRecord(key, record.value()));
        }
      }
    }
  }


  @Override
  public Optional<OffsetRecord> findLatestOffsetRecord(FileKey key) {
    try {
      awaitReady();
      return this.offsetStorage.find(key);
    } catch (ConditionTimeoutException e) {
      throw new IllegalStateException("OffsetManager initialization timed out");
    }
  }

  @Override
  public List<OffsetRecord> findLatestOffsetRecords(List<FileKey> keys) {
    try {
      awaitReady();
      return keys.stream()
        .map(offsetStorage::find)
        .flatMap(Optional::stream)
        .toList();
    } catch (ConditionTimeoutException e) {
      throw new IllegalStateException("OffsetManager initialization timed out");
    }
  }

  private void awaitReady() {
    try {
      Awaitility.await()
        .atMost(Duration.ofSeconds(10))
        .pollDelay(Duration.ZERO)
        .pollInterval(Duration.ofSeconds(1))
        .untilTrue(isReady);
    } catch (ConditionTimeoutException e) {
      throw new IllegalStateException("OffsetManager initialization timed out");
    }
  }

  @Override
  public void upsert(FileKey key, OffsetRecord offsetRecord) {
    this.offsetStorage.upsert(key, offsetRecord);
  }

  @Override
  public void removeKey(FileKey key) {
    this.offsetStorage.remove(key);
  }

  @PreDestroy
  public void shutdown() {
    isRunning.set(false);
    log.info("Shutting down OffsetManager...");
    Consumer<String, Long> consumer = this.activeConsumer.getAndSet(null);
    if (consumer != null) {
      consumer.wakeup(); // interrupts Consumer Poll()
    }
    this.executorService.shutdownNow(); // Allow run() to exit in executorService
    try {
      if (!this.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        log.warn("Executor did not terminate cleanly");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
