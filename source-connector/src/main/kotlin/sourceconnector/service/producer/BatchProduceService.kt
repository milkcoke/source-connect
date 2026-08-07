package sourceconnector.service.producer

import offsetmanager.domain.offset.OffsetRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import sourceconnector.domain.batch.MessageBatch
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.ByteArray
import kotlin.Exception
import kotlin.String

class BatchProduceService(
  properties: Properties,
  private val logTopic: String,
  private val offsetTopic: String
) : BatchProducer<String> {
  private val log = LoggerFactory.getLogger(BatchProduceService::class.java)
  private val kafkaProducer: KafkaProducer<String?, ByteArray?> = KafkaProducer(properties)

  init {
    this.kafkaProducer.initTransactions()
  }

  override fun sendBatch(
    offsetRecord: OffsetRecord,
    messageBatch: MessageBatch<String>
  ): BatchResult {
    val batch: Collection<String> = messageBatch.get()

    try {
      this.kafkaProducer.beginTransaction()

      for (message in batch) {
        this.kafkaProducer.send(
          ProducerRecord(
            this.logTopic,
            null,
            message.toByteArray(StandardCharsets.UTF_8)
          )
        )
      }
      this.kafkaProducer.send(
        ProducerRecord(
          this.offsetTopic,
          offsetRecord.key().get(),
          ByteBuffer.allocate(java.lang.Long.BYTES).putLong(offsetRecord.offset()).array()
        )
      )

      this.kafkaProducer.commitTransaction()
    } catch (e: Exception) {
      log.error("Abort transaction since {}", e.message)
      this.kafkaProducer.abortTransaction()
      return BatchResult.FAIL
    }
    return BatchResult.SUCCESS
  }

  override fun close() {
    this.kafkaProducer.close()
  }
}
