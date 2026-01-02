package offsetmanager.domain

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import offsetmanager.domain.file.factory.FileKeyParser
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.exception.OffsetManagerNotReadyException
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.PartitionInfo
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.utils.Utils
import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class OffsetStateUpdaterImpl(
private val offsetTopicName: String,
  private val consumerProperties: Properties,
  private val offsetStorage: OffsetStorage,
  private val executorService: ExecutorService = Executors.newSingleThreadExecutor(),
  private val activeConsumer: AtomicReference<Consumer<String, Long>> = AtomicReference(),
    /**
     * Indicates whether the background consumer loop is running
     * This is used for shutdown and restart logic
     */
  private val isRunning: AtomicBoolean = AtomicBoolean(true),

    /**
     * Indicates whether the OffsetStateUpdater is ready to serve requests
     */
  private val isReady: AtomicBoolean = AtomicBoolean(false)
) : OffsetStateUpdater {

  private var assignedPartitionInfos: Set<PartitionInfo> = mutableSetOf()
  private val log = LoggerFactory.getLogger(this::class.java)

  @PostConstruct
  override fun start() {
    this.executorService.submit { this.runLoop() }
  }


  private fun runLoop() {
    while (isRunning.get()) {
      // reset readiness BEFORE starting a new consumer

      isReady.set(false)
      isRunning.set(false)
      this.activeConsumer.set(null)
      this.offsetStorage.clear()

      try {
        KafkaConsumer<String, Long>(consumerProperties).use { consumer ->
          log.info("Starting OffsetManager consumer")
          val partitions = discoverPartitions(consumer, offsetTopicName)
          this.assignedPartitionInfos = getAssignedPartitionInfos(consumer, offsetTopicName)
          this.activeConsumer.set(consumer)

          initializeSnapshot(consumer, partitions)

          // publish readiness
          isRunning.set(true)
          isReady.set(true)
          log.info("OffsetManager is ready state")

          // blocks until exception or shutdown
          streamUpdates(consumer)
        }
      } catch (e: IllegalStateException) {
        log.error("IllegalStateException: {}", e.message)
        Utils.sleep(3000) // backoff before retry
      } catch (e: WakeupException) {
        if (!isRunning.get()) {
          log.info("OffsetManager shutting down")
          return
        }
        log.warn("WakeupException during run loop", e)
      } catch (e: Exception) {
        // RECOVERABLE failure
        log.warn("Consumer failed", e)
        Utils.sleep(3000) // backoff before retry
      }
    }
  }

  private fun initializeSnapshot(consumer: Consumer<String, Long?>, topicPartitions: List<TopicPartition>) {
    // 1. Assign all partitions

    consumer.assign(topicPartitions)

    // 2. Barrier step for initializing fetch session to complete, Checking topic partition is ready
    consumer.poll(Duration.ZERO)

    // 3. Get the end offsets
    val endOffsets: Map<TopicPartition, Long> = consumer.endOffsets(topicPartitions)

    // 4. Seek to the beginning
    consumer.seekToBeginning(topicPartitions)

//     5. Processing all records until reaching end offsets
    val remaining: MutableSet<TopicPartition> = HashSet(topicPartitions)
    while (remaining.isNotEmpty()) {
      val records = consumer.poll(Duration.ofMillis(100))

      for (record in records) {
        val fileKey = FileKeyParser.parse(record.key())
        this.offsetStorage.upsert(fileKey, DefaultOffsetRecord(fileKey, record.value()!!))
      }

      // Check progress only on unfinished partitions
      val it = remaining.iterator()
      while (it.hasNext()) {
        val tp = it.next()
        val currentOffset = consumer.position(tp)
        val endOffset: Long = endOffsets[tp]!!

        if (currentOffset >= endOffset) {
          it.remove()
        }
      }
    }

    log.info("Snapshot initialization completed for all partitions")
  }

  private fun discoverPartitions(consumer: Consumer<*, *>, topic: String): List<TopicPartition> {
    val infos = consumer.partitionsFor(topic)

    check(!(infos == null || infos.isEmpty())) { "Topic not available: $topic" }

    return infos.stream()
      .map<TopicPartition> { p: PartitionInfo -> TopicPartition(p.topic(), p.partition()) }
      .toList()
  }

  private fun getAssignedPartitionInfos(consumer: Consumer<*, *>, topic: String): Set<PartitionInfo> {
    val infos = consumer.partitionsFor(topic)

    check(!(infos == null || infos.isEmpty())) { "Topic not available: $topic" }

    return HashSet<PartitionInfo>(infos)
  }

  private fun needsUpdateConsumer(consumer: Consumer<*, *>): Boolean {
    val currentPartitionInfos = consumer.partitionsFor(offsetTopicName)

    if (currentPartitionInfos == null || currentPartitionInfos.isEmpty()) {
      return true
    }

    val currentPartitionInfoSet: Set<PartitionInfo> = HashSet(currentPartitionInfos)

    return this.assignedPartitionInfos != currentPartitionInfoSet
  }

  private fun streamUpdates(consumer: Consumer<String, Long?>) {
    while (isRunning.get()) {
      val records = consumer.poll(Duration.ofMillis(100))

      for (record in records) {
        val key = FileKeyParser.parse(record.key()!!)
        if (record.value() == null) {
          this.offsetStorage.remove(key)
        } else {
          this.offsetStorage.upsert(key, DefaultOffsetRecord(key, record.value()!!))
        }
      }

      check(!needsUpdateConsumer(consumer)) { "Offset Topic metadata is invalid, Consumer should be recreated." }
    }
  }

  @PreDestroy
  override fun stop() {
    isRunning.set(false)
    isReady.set(false)
    log.info("Shutting down OffsetManager...")
    val consumer = this.activeConsumer.getAndSet(null)
    if (consumer != null) {
      consumer.wakeup() // interrupts Consumer Poll()
    }
    this.executorService.shutdownNow() // Allow run() to exit in executorService
    try {
      if (!this.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        log.warn("Executor did not terminate cleanly")
      }
    } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
    }
  }

  override fun awaitReady() {
    try {
      Awaitility.await()
        .atMost(Duration.ofSeconds(30))
        .pollDelay(Duration.ZERO)
        .pollInterval(Duration.ofSeconds(1))
        .untilTrue(isReady)
    } catch (e: ConditionTimeoutException) {
      throw OffsetManagerNotReadyException()
    }
  }
}
