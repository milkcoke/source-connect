package sourceconnector.repository.offset

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.factory.FileKeyParser
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetRecord
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.utils.Utils
import org.slf4j.LoggerFactory
import sourceconnector.service.offset.OffsetRecordRepository
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.ExecutionException

class InternalOffsetRecordRepository(
  private val consumer: Consumer<String, Long>,
  private val adminClient: AdminClient,
  private val offsetTopic: String,
  private val timeout: Duration = Duration.ofMillis(100)
) : OffsetRecordRepository {

  private val log = LoggerFactory.getLogger(InternalOffsetRecordRepository::class.java)

  override fun findLastOffsetRecord(key: FileKey): OffsetRecord? {
    val partition = this.getPartitionsForTopic(key)
    val topicPartition = TopicPartition(offsetTopic, partition)
    this.consumer.assign(listOf(topicPartition))
    val beginOffset: Long = this.consumer.beginningOffsets(listOf(topicPartition))[topicPartition]!!
    val endOffset: Long = this.consumer.endOffsets(listOf(topicPartition))[topicPartition]!!

    if (beginOffset >= endOffset) {
      return null
    }

    consumer.seek(topicPartition, beginOffset)
    var lastOffsetRecord: OffsetRecord? = null

    var lastConsumedOffset: Long = Long.MIN_VALUE
    while (lastConsumedOffset < endOffset) {

      val recordList = this.consumer
        .poll(timeout)
        .records(topicPartition)

      lastOffsetRecord = recordList
        .filter { it.key() == key.get() }
        .maxBy { record: ConsumerRecord<String, Long> -> record.offset() }
        .let {
          DefaultOffsetRecord(
            FileKeyParser.parse(it.key()),
            it.value()
          )
        }

      lastConsumedOffset = consumer.position(topicPartition)
    }

    return lastOffsetRecord
  }

  override fun findLastOffsetRecords(keys: List<FileKey>): List<OffsetRecord> {
    if (keys.isEmpty()) return  emptyList()

    val keysByPartition: Map<Int, List<FileKey>> = keys.groupBy { fileKey -> getPartitionsForTopic(fileKey) }

    val keyOffsetMap: MutableMap<FileKey, OffsetRecord> = mutableMapOf()
    // Iterate through each partition
    for ((partition, fileKeys) in keysByPartition.entries) {
      val fileKeySet: Set<FileKey> = fileKeys.toSet()

      val topicPartition = TopicPartition(offsetTopic, partition)
      this.consumer.assign(listOf(topicPartition))
      val beginOffset: Long = this.consumer.beginningOffsets(listOf(topicPartition))[topicPartition]!!
      val endOffset: Long = this.consumer.endOffsets(listOf(topicPartition))[topicPartition]!!

      if (beginOffset >= endOffset) continue

      consumer.seek(topicPartition, beginOffset)

      var lastConsumedOffset: Long = Long.MIN_VALUE
      while (lastConsumedOffset < endOffset) {
        val recordList: List<ConsumerRecord<String, Long>> = this.consumer
          .poll(timeout)
          .records(topicPartition)

        for (record in recordList) {
          val fileKey = FileKeyParser.parse(record.key())
          if (!fileKeySet.contains(fileKey)) continue
          val offset: Long = record.value()!!
          keyOffsetMap[fileKey] = DefaultOffsetRecord(fileKey, offset)
        }
        lastConsumedOffset = consumer.position(topicPartition)
      }
    }

    return keyOffsetMap.values.toList()
  }

  private fun getPartitionsForTopic(fileKey: FileKey): Int {
    // get partition count of topic
    val result = adminClient.describeTopics(listOf(offsetTopic))
    val futures = result.topicNameValues()
    try {
      val description = futures[offsetTopic]!!.get()
      val numPartitions = description.partitions().size
      val serializedKey = fileKey.get().toByteArray(StandardCharsets.UTF_8)
      return Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions
    } catch (e: ExecutionException) {
      log.error("Failed to get partitions for topic {}", offsetTopic, e)
      throw RuntimeException(e.message)
    } catch (e: InterruptedException) {
      log.error("Failed to get partitions for topic {}", offsetTopic, e)
      throw RuntimeException(e.message)
    }
  }

  @Throws(Exception::class)
  override fun close() {
    this.consumer.close()
    this.adminClient.close()
  }
}
