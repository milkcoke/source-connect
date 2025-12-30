package sourceconnector

import offsetmanager.domain.file.LocalFileKey.Companion.from
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.domain.batch.MessageBatch
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.LogMetadata
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder
import sourceconnector.domain.pipeline.factory.PipelineBuilder
import sourceconnector.domain.processor.impl.EmptyFilterProcessor
import sourceconnector.domain.processor.impl.TrimMapperProcessor
import sourceconnector.repository.file.LocalFileRepository
import sourceconnector.service.batcher.Batchable
import sourceconnector.service.batcher.LogBatcher
import sourceconnector.service.producer.BatchProduceService
import sourceconnector.support.KafkaTestSupport
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal class SourceConnectorTest : KafkaTestSupport() {
  private val logTopic = "temp-log-test"
  private val offsetTopic = "temp-offset-test"

  @DisplayName("Send batch")
  @Test
  @Throws(Exception::class)
  fun mainTest() {
    val pipelineBuilder: PipelineBuilder<Log> = FileBaseLogPipelineBuilder()
    val localPath = Path.of("src/test/resources/sample-data/large.ndjson")

    val pipeline = pipelineBuilder.create(
      LocalFileRepository(),
      from(localPath),
      JSONLogFactory(),
      listOf(TrimMapperProcessor(JSONLogFactory()), EmptyFilterProcessor())
    )
    val batcher: Batchable<Log> = LogBatcher(pipeline, 10000)

    BatchProduceService(producerProperties, logTopic, offsetTopic).use { producer ->
      // when
      var lastMessageMetadata = LogMetadata.EMPTY
      while (batcher.hasNextBatch()) {
        val messages = batcher.nextBatch().get()
        if (messages.isEmpty()) continue
        lastMessageMetadata = messages.last().metadata
        val messageBatch = messages
          .stream()
          .map<String> { obj: Log? -> obj!!.get() }
          .toList()
        producer.sendBatch(
          DefaultOffsetRecord(
            lastMessageMetadata.key,
            lastMessageMetadata.offset
          )
        ) { messageBatch }
      }
      if (lastMessageMetadata !== LogMetadata.EMPTY) {
        producer.sendBatch(
          DefaultOffsetRecord(
            lastMessageMetadata.key,
            OffsetStatus.COMPLETED.offset
          ), MessageBatch { mutableListOf() })
      }
    }
  }

  @DisplayName("Should handle multiple files")
  @Test
  @Throws(Exception::class)
  fun handleDirectory() {
    // given
    BatchProduceService(producerProperties, logTopic, offsetTopic).use { producer ->
      Files.walk(Paths.get("src/test/resources/sample-data")).use { stream ->
        val files: List<File> = stream
          .filter { path: Path -> Files.isRegularFile(path) }
          .filter { p: Path -> p.toString().endsWith(".ndjson") }
          .map<File> { obj: Path -> obj.toFile() }
          .toList()

        // then
        val pipelineBuilder: PipelineBuilder<Log> = FileBaseLogPipelineBuilder()
        for (file in files) {
          val pipeline = pipelineBuilder.create(
            LocalFileRepository(),
            from(file.toPath()),
            JSONLogFactory(),
            listOf(TrimMapperProcessor(JSONLogFactory()), EmptyFilterProcessor())
          )

          val batcher: Batchable<Log> = LogBatcher(pipeline, 10000)

          var lastMessageMetadata = LogMetadata.EMPTY
          while (batcher.hasNextBatch()) {
            val messages = batcher.nextBatch().get()
            if (messages.isEmpty()) continue
            lastMessageMetadata = messages.last().metadata
            val messageBatch = messages
              .stream()
              .map<String> { obj: Log? -> obj!!.get() }
              .toList()
            producer.sendBatch(
              DefaultOffsetRecord(
                lastMessageMetadata.key,
                lastMessageMetadata.offset
              )
            ) { messageBatch }
          }

          if (lastMessageMetadata !== LogMetadata.EMPTY) {
            producer.sendBatch(
              DefaultOffsetRecord(
                lastMessageMetadata.key,
                OffsetStatus.COMPLETED.offset
              )
            ) { mutableListOf() }
          }
        }
      }
    }
  }

  @DisplayName("Nothing to produce after completes")
  @Test
  @Throws(Exception::class)
  fun nothingToDoAfterProcessingAllFiles() {
    // given
    BatchProduceService(producerProperties, logTopic, offsetTopic).use { producer ->
      Files.walk(Paths.get("src/test/resources/sample-data")).use { stream ->
        val files: List<File> = stream
          .filter { path: Path -> Files.isRegularFile(path) }
          .filter { p: Path -> p.toString().endsWith(".ndjson") }
          .map<File> { obj: Path -> obj.toFile() }
          .toList()

        // then
        val pipelineBuilder: PipelineBuilder<Log> = FileBaseLogPipelineBuilder()
        for (file in files) {
          val pipeline = pipelineBuilder.create(
            LocalFileRepository(),
            from(file.toPath()),
            JSONLogFactory(),
            listOf(TrimMapperProcessor(JSONLogFactory()), EmptyFilterProcessor())
          )

          val batcher: Batchable<Log> = LogBatcher(pipeline, 10000)

          var lastMessageMetadata = LogMetadata.EMPTY
          while (batcher.hasNextBatch()) {
            val messages = batcher.nextBatch().get()
            if (messages.isEmpty()) continue
            lastMessageMetadata = messages.last().metadata
            val messageBatch = messages
              .stream()
              .map<String> { obj: Log? -> obj!!.get() }
              .toList()
            producer.sendBatch(
              DefaultOffsetRecord(
                lastMessageMetadata.key,
                lastMessageMetadata.offset
              )
            ) { messageBatch }
          }

          if (lastMessageMetadata !== LogMetadata.EMPTY) {
            producer.sendBatch(
              DefaultOffsetRecord(
                lastMessageMetadata.key,
                OffsetStatus.COMPLETED.offset
              )
            ) { mutableListOf() }
          }
        }
      }
    }
  }
}
