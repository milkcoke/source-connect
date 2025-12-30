package sourceconnector.domain.pipeline.factory

import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.domain.log.factory.JSONLogFactory
import sourceconnector.domain.log.factory.LogFactory
import sourceconnector.domain.processor.impl.EmptyFilterProcessor
import sourceconnector.domain.processor.impl.TrimMapperProcessor
import sourceconnector.repository.file.LocalFileRepository
import java.nio.file.Path

internal class FileBaseLogPipelineBuilderTest {
  @DisplayName("Should throw IllegalArgumentException when processors are not provided")
  @Test
  fun createMissingProcessorTest() {
    // given
    val builder = FileBaseLogPipelineBuilder()
    val localPath = Path.of("src/test/resources/sample-data/large.ndjson")
    val logFactory: LogFactory = JSONLogFactory()
    // when then
    assertThatThrownBy {
      builder.create(
        LocalFileRepository(),
        from(localPath),
        logFactory,
        emptyList()
      )
    }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("processors is required")
  }

  @DisplayName("Should create pipeline consists of processors")
  @Test
  fun pipelineCreateTest() {
    // given
    val builder = FileBaseLogPipelineBuilder()
    val path = Path.of("src/test/resources/sample-data/large.ndjson")
    val logFactory: LogFactory = JSONLogFactory()
    // when then
    assertDoesNotThrow {
      builder.create(
        LocalFileRepository(),
        from(path),
        logFactory,
        listOf(TrimMapperProcessor(logFactory), EmptyFilterProcessor())
      )
    }
  }

  @DisplayName("Should throw IllegalStateException when failing open the file")
  @Test
  fun failToCreatePipelineTest() {
    // given
    val builder = FileBaseLogPipelineBuilder()
    val invalidPath = Path.of("invalidPath")
    val logFactory: LogFactory = JSONLogFactory()

    // when then
    assertThatThrownBy {
      builder.createWithNoProcessor(
        LocalFileRepository(),
        from(invalidPath),
        logFactory
      )
    }.isInstanceOf(IllegalStateException::class.java)
      .hasMessageContaining("Failed to create pipeline for file")
  }

  @DisplayName("Should create pipeline with no processors")
  @Test
  fun createWithNoProcessor() {
    // given
    val builder = FileBaseLogPipelineBuilder()
    val localPath = Path.of("src/test/resources/sample-data/large.ndjson")
    val logFactory: LogFactory = JSONLogFactory()

    // when then
    assertDoesNotThrow {
      builder.createWithNoProcessor(
        LocalFileRepository(),
        from(localPath),
        logFactory
      )
    }
  }

  @DisplayName("Should throw NoSuchElementException when trying getResult even though pipeline is complete")
  @Test
  fun tryingGetResultCompletedPipelineTest() {
    // given
    val builder = FileBaseLogPipelineBuilder()
    val logFactory: LogFactory = JSONLogFactory()
    val localPath = Path.of("src/test/resources/sample-data/empty.ndjson")

    val pipeline = builder.createWithNoProcessor(
      LocalFileRepository(),
      from(localPath),
      logFactory
    )
    pipeline.getResult()
    assertThat(pipeline.isComplete).isTrue()
    // when then
    assertThatThrownBy { pipeline.getResult() }
      .isInstanceOf(NoSuchElementException::class.java)
      .hasMessage("No more data")
  }
}
