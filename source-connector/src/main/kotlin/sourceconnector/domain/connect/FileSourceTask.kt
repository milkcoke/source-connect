package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetStatus
import org.slf4j.LoggerFactory
import sourceconnector.domain.log.Log
import sourceconnector.domain.pipeline.factory.PipelineSupplier
import sourceconnector.service.batcher.LogBatcher
import sourceconnector.service.producer.BatchProducer
import sourceconnector.service.producer.ProduceResult

class FileSourceTask(
  override val index: Int,
  private val pipelineSupplier: PipelineSupplier<Log>,
  private val producer: BatchProducer<String>,
  private val id: String = String.format("Task-%d", index)
) : Task<FileProcessingResult> {

  private val log = LoggerFactory.getLogger(FileSourceTask::class.java)
  // visible for test
  internal val fileKeyOffsetMap: MutableMap<FileKey, Long> = mutableMapOf()
  private lateinit var result: FileProcessingResult

  @Throws(Exception::class)
  override fun call(): FileProcessingResult {
    try {
      for ((fileKey, offset) in this.fileKeyOffsetMap) {
        if (offset == OffsetStatus.COMPLETED.offset) {
          result.addSkippedCount()
          continue
        }

        if (processFile(fileKey, offset).isFailure) {
          log.error("Failed to produce file: {}", fileKey.get())
          this.result.addFailure(fileKey)
        } else {
          this.result.addSuccessCount()
        }
      }

      return this.result
    } finally {
      this.producer.close()
    }
  }

  override fun assign(offsetMap: Map<FileKey, Long>) {
    this.fileKeyOffsetMap.putAll(offsetMap)
    this.result = FileProcessingResult(offsetMap.size)
  }

  private fun processFile(fileKey: FileKey, offset: Long): ProduceResult {
    pipelineSupplier.get(fileKey).use { pipeline ->
      pipeline.toPosition(offset)
      val batcher = LogBatcher(pipeline, 10000)

      while (batcher.hasNextBatch()) {
        val messages = batcher.nextBatch().get()
        if (messages.isEmpty()) continue

        val lastMetadata = messages.last().metadata
        val batch = messages.map { it.get() }

        val result: ProduceResult = producer.sendBatch(
          DefaultOffsetRecord(lastMetadata.key, lastMetadata.offset)
        ) { batch }

        // Early return if fail before completing process
        if (result.isFailure) return result
      }

      return producer.sendBatch(
        DefaultOffsetRecord(fileKey, OffsetStatus.COMPLETED.offset)
      ) { emptyList() }
    }
  }
}
