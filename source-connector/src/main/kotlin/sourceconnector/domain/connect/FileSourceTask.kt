package sourceconnector.domain.connect

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetStatus
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.LogMetadata
import sourceconnector.domain.pipeline.factory.PipelineSupplier
import sourceconnector.service.batcher.LogBatcher
import sourceconnector.service.producer.BatchProducer
import kotlin.collections.mutableListOf

class FileSourceTask(
  override val index: Int,
  private val pipelineSupplier: PipelineSupplier<Log>,
  private val producer: BatchProducer<String>,
  private val id: String = String.format("Task-%d", index)
) : Task<FileProcessingResult> {

  // visible for test
  internal val fileKeyOffsetMap: MutableMap<FileKey, Long> = mutableMapOf()
  private lateinit var result: FileProcessingResult

  @Throws(Exception::class)
  override fun call(): FileProcessingResult {
    try {
      for (entry in this.fileKeyOffsetMap.entries) {
        val offset: Long = entry.value
        if (offset == OffsetStatus.COMPLETED.offset) {
          result.addSkippedCount()
          continue
        }
        val fileKey: FileKey = entry.key
        val pipeline = pipelineSupplier.get(fileKey)

        // Progress offset to the next position in the file
        pipeline.toPosition(offset)

        val batcher = LogBatcher(pipeline, 10000)

        var lastMessageMetadata: LogMetadata

        while (batcher.hasNextBatch()) {
          val messages = batcher.nextBatch().get()
          if (messages.isEmpty()) continue
          lastMessageMetadata = messages.last().metadata
          val messageBatch: List<String> = messages
            .map { log -> log.get() }
            .toList()

          producer.sendBatch(
            DefaultOffsetRecord(
              lastMessageMetadata.key,
              lastMessageMetadata.offset
            )
          ) { messageBatch }
        }


        // Complete this file
        producer.sendBatch(
          DefaultOffsetRecord( // This is for handling no Log after filtered
            fileKey,
            OffsetStatus.COMPLETED.offset
          )
        ) { emptyList() }

        this.result.addSuccessCount()
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
}
