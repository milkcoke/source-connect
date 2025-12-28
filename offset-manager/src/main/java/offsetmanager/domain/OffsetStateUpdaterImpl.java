package offsetmanager.domain;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.exception.OffsetManagerNotReadyException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.kafka.common.utils.Utils.sleep;

@RequiredArgsConstructor
@Slf4j
public class OffsetStateUpdaterImpl implements OffsetStateUpdater {
  private final String offsetTopicName;
  private final Properties consumerProperties;
  private final OffsetStorage offsetStorage;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private final AtomicReference<Consumer<String, Long>> activeConsumer = new AtomicReference<>();
  /**
   * indicates whether the background consumer loop is running
   * This is used for shutdown and restart logic
   */
  private final AtomicBoolean isRunning = new AtomicBoolean(true);
  /**
   * indicates whether the OffsetStateUpdater is ready to serve requests
   */
  private final AtomicBoolean isReady = new AtomicBoolean(false);

  private Set<PartitionInfo> assignedPartitionInfos = Collections.emptySet();

  @PostConstruct
  @Override
  public void start() {
    this.executorService.submit(this::runLoop);
  }


  private void runLoop() {
    while (isRunning.get()) {

      // reset readiness BEFORE starting a new consumer
      isReady.set(false);
      isRunning.set(false);
      this.activeConsumer.set(null);
      this.offsetStorage.clear();

      try (Consumer<String, Long> consumer = new KafkaConsumer<>(consumerProperties)) {
        log.info("Starting OffsetManager consumer");

        List<TopicPartition> partitions = discoverPartitions(consumer, offsetTopicName);
        this.assignedPartitionInfos = getAssignedPartitionInfos(consumer, offsetTopicName);
        this.activeConsumer.set(consumer);

        initializeSnapshot(consumer, partitions);

        // publish readiness
        isRunning.set(true);
        isReady.set(true);
        log.info("OffsetManager is ready state");

        // blocks until exception or shutdown
        streamUpdates(consumer);

      } catch (IllegalStateException e) {
        log.error("IllegalStateException: {}", e.getMessage());
        sleep(3000); // backoff before retry
      } catch (WakeupException e) {
        if (!isRunning.get()) {
          log.info("OffsetManager shutting down");
          return;
        }
        log.warn("WakeupException during run loop", e);

      } catch (Exception e) {
        // RECOVERABLE failure
        log.warn("Consumer failed", e);
        sleep(3000); // backoff before retry

      }
    }
  }

  private void initializeSnapshot(Consumer<String, Long> consumer, List<TopicPartition> topicPartitions) {

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
        this.offsetStorage.upsert(fileKey, new DefaultOffsetRecord(fileKey, record.value()));
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

  private List<TopicPartition> discoverPartitions(Consumer<?, ?> consumer, String topic) {
    List<PartitionInfo> infos = consumer.partitionsFor(topic);

    if (infos == null || infos.isEmpty()) {
      throw new IllegalStateException("Topic not available: " + topic);
    }

    return infos.stream()
      .map(p -> new TopicPartition(p.topic(), p.partition()))
      .toList();
  }

  private Set<PartitionInfo> getAssignedPartitionInfos(Consumer<?, ?> consumer, String topic) {
    List<PartitionInfo> infos = consumer.partitionsFor(topic);

    if (infos == null || infos.isEmpty()) {
      throw new IllegalStateException("Topic not available: " + topic);
    }

    return new HashSet<>(infos);
  }

  private boolean needsUpdateConsumer(Consumer<?, ?> consumer) {
    List<PartitionInfo> currentPartitionInfos = consumer.partitionsFor(offsetTopicName);

    if (currentPartitionInfos == null || currentPartitionInfos.isEmpty()) {
      return true;
    }

    Set<PartitionInfo> currentPartitionInfoSet = new HashSet<>(currentPartitionInfos);

    return !this.assignedPartitionInfos.equals(currentPartitionInfoSet);
  }

  private void streamUpdates(Consumer<String, Long> consumer) {
    while (isRunning.get()) {
      ConsumerRecords<String, Long> records =
        consumer.poll(Duration.ofMillis(100));

      for (ConsumerRecord<String, Long> record : records) {
        FileKey key = FileKeyParser.parse(record.key());
        if (record.value() == null) {
          this.offsetStorage.remove(key);
        } else {
          this.offsetStorage.upsert(key, new DefaultOffsetRecord(key, record.value()));
        }
      }

      if (needsUpdateConsumer(consumer)) {
        throw new IllegalStateException("Offset Topic metadata is invalid, Consumer should be recreated.");
      };
    }
  }

  @Override
  @PreDestroy
  public void stop() {
    isRunning.set(false);
    isReady.set(false);
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

  @Override
  public void awaitReady() {
    try {
      Awaitility.await()
        .atMost(Duration.ofSeconds(30))
        .pollDelay(Duration.ZERO)
        .pollInterval(Duration.ofSeconds(1))
        .untilTrue(isReady);
    } catch (ConditionTimeoutException e) {
      throw new OffsetManagerNotReadyException();
    }
  }

}
