package sourceconnector.domain.pipeline.factory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.log.factory.LogFactory;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.processor.impl.EmptyFilterProcessor;
import sourceconnector.domain.processor.impl.TrimMapperProcessor;
import sourceconnector.repository.file.LocalFileRepository;

import java.io.File;
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
    File file = Path.of("src/test/resources/sample-data/large.ndjson").toFile();
    LogFactory logFactory = new JSONLogFactory();
    // when then
    assertThatThrownBy(() ->
        builder.create(
          new LocalFileRepository(),
          file.getAbsolutePath(),
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
    File file = Path.of("src/test/resources/sample-data/large.ndjson").toFile();
    LogFactory logFactory = new JSONLogFactory();
    // when then
    assertDoesNotThrow(() -> {
      builder.create(
        new LocalFileRepository(),
        file.getAbsolutePath(),
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
    File file = Path.of("invalidPath").toFile();
    LogFactory logFactory = new JSONLogFactory();

    // when then
    assertThatThrownBy(()->
        builder.createWithNoProcessor(
          new LocalFileRepository(),
          file.getAbsolutePath(),
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
    File file = Path.of("src/test/resources/sample-data/large.ndjson").toFile();
    LogFactory logFactory = new JSONLogFactory();

    // when then
    assertDoesNotThrow(() -> {
      builder.createWithNoProcessor(
        new LocalFileRepository(),
        file.getAbsolutePath(),
        logFactory
      );
    });
  }

  @DisplayName("Should throw NoSuchElementException when trying getResult even though pipeline is complete")
  @Test
  void tryingGetResultCompletedPipelineTest() {
    // given
    FileBaseLogPipelineBuilder builder = new FileBaseLogPipelineBuilder();
    File file = Path.of("src/test/resources/sample-data/empty.ndjson").toFile();
    LogFactory logFactory = new JSONLogFactory();

    Pipeline<Log>  pipeline = builder.createWithNoProcessor(
      new LocalFileRepository(),
      file.getAbsolutePath(),
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
