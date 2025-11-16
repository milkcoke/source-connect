package sourceconnector.domain.pipeline.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.file.LocalFileKey;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.log.factory.LogFactory;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.processor.impl.EmptyFilterProcessor;
import sourceconnector.domain.processor.impl.TrimMapperProcessor;
import sourceconnector.repository.file.LocalFileRepository;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class FileBaseLogPipelineBuilderTest {

  @DisplayName("Should throw IllegalArgumentException when processors are not provided")
  @Test
  void createMissingProcessorTest() {
    // given
    FileBaseLogPipelineBuilder builder = new FileBaseLogPipelineBuilder();
    Path localPath = Path.of("src/test/resources/sample-data/large.ndjson");
    LogFactory logFactory = new JSONLogFactory();
    // when then
    assertThatThrownBy(() ->
        builder.create(
          new LocalFileRepository(),
          LocalFileKey.from(localPath),
          logFactory,
          Collections.emptyList()
        ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("processors is required");
  }

  @DisplayName("Should create pipeline consists of processors")
  @Test
  void pipelineCreateTest() {
    // given
    FileBaseLogPipelineBuilder builder = new FileBaseLogPipelineBuilder();
    Path path = Path.of("src/test/resources/sample-data/large.ndjson");
    LogFactory logFactory = new JSONLogFactory();
    // when then
    assertDoesNotThrow(() -> {
      builder.create(
        new LocalFileRepository(),
        LocalFileKey.from(path),
        logFactory,
        List.of(new TrimMapperProcessor(logFactory), new EmptyFilterProcessor())
      );
    });

  }

  @DisplayName("Should throw IllegalStateException when failing open the file")
  @Test
  void failToCreatePipelineTest() {
    // given
    FileBaseLogPipelineBuilder builder = new FileBaseLogPipelineBuilder();
    Path invalidPath = Path.of("invalidPath");
    LogFactory logFactory = new JSONLogFactory();

    // when then
    assertThatThrownBy(()->
        builder.createWithNoProcessor(
          new LocalFileRepository(),
          LocalFileKey.from(invalidPath),
          logFactory
        )
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Failed to create pipeline for file");
  }

  @DisplayName("Should create pipeline with no processors")
  @Test
  void createWithNoProcessor() {
    // given
    FileBaseLogPipelineBuilder builder = new FileBaseLogPipelineBuilder();
    Path localPath = Path.of("src/test/resources/sample-data/large.ndjson");
    LogFactory logFactory = new JSONLogFactory();

    // when then
    assertDoesNotThrow(() -> {
      builder.createWithNoProcessor(
        new LocalFileRepository(),
        LocalFileKey.from(localPath),
        logFactory
      );
    });
  }

  @DisplayName("Should throw NoSuchElementException when trying getResult even though pipeline is complete")
  @Test
  void tryingGetResultCompletedPipelineTest() {
    // given
    FileBaseLogPipelineBuilder builder = new FileBaseLogPipelineBuilder();
    LogFactory logFactory = new JSONLogFactory();
    Path localPath = Path.of("src/test/resources/sample-data/empty.ndjson");

    Pipeline<Log>  pipeline = builder.createWithNoProcessor(
      new LocalFileRepository(),
      LocalFileKey.from(localPath),
      logFactory
    );

    pipeline.getResult();
    assertThat(pipeline.isComplete()).isTrue();
    // when then
    assertThatThrownBy(pipeline::getResult)
      .isInstanceOf(NoSuchElementException.class)
      .hasMessage("No more data");
  }
}
