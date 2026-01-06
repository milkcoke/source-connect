package sourceconnector.service.batcher

import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.domain.log.Log
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder
import sourceconnector.domain.pipeline.factory.PipelineBuilder
import sourceconnector.domain.processor.impl.EmptyFilterProcessor
import sourceconnector.domain.processor.impl.TrimMapperProcessor
import sourceconnector.repository.file.LocalFileRepository
import java.nio.file.Path
import java.util.*

internal class LogBatcherTest {
  @DisplayName("Should return emptyList when no more logs to batch")
  @Test
  fun nextBatchAtEmptyFile() {
    // given
    val path = Path.of("src/test/resources/sample-data/empty.ndjson")

    val pipelineBuilder: PipelineBuilder<Log> = FileBaseLogPipelineBuilder()
    val pipeline = pipelineBuilder.create(
      LocalFileRepository(),
      from(path),
      JSONLogFactory(),
      listOf(TrimMapperProcessor(JSONLogFactory()), EmptyFilterProcessor())
    )
    val batcher: Batchable<Log> = LogBatcher(pipeline, 100)

    // when
    val batch = batcher.nextBatch()

    // then
    assertThat<Log?>(batch.get()).isEqualTo(Collections.EMPTY_LIST)
  }

  @DisplayName("Should get batch according to the Batcher batch size")
  @Test
  fun nextBatchTest() {
    val path = Path.of("src/test/resources/sample-data/empty-included.ndjson")

    val pipelineBuilder: PipelineBuilder<Log> = FileBaseLogPipelineBuilder()
    val pipeline = pipelineBuilder.create(
      LocalFileRepository(),
      from(path),
      JSONLogFactory(),
      listOf(TrimMapperProcessor(JSONLogFactory()), EmptyFilterProcessor())
    )
    val batcher: Batchable<Log> = LogBatcher(pipeline, 3)

    // when
    val batch = batcher.nextBatch()

    // then
    assertThat<Log>(batch.get()).hasSize(3)
  }
}
