package sourceconnector.service.batcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.batch.MessageBatch;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder;
import sourceconnector.domain.pipeline.factory.PipelineBuilder;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.processor.impl.EmptyFilterProcessor;
import sourceconnector.domain.processor.impl.TrimMapperProcessor;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogBatcherTest {

  @DisplayName("Should return emptyList when no more logs to batch")
  @Test
  void nextBatchAtEmptyFile() {
    // given
    File file = Path.of("src/test/resources/sample-data/empty.ndjson").toFile();
    PipelineBuilder<Log> pipelineBuilder = new FileBaseLogPipelineBuilder();
    Pipeline<Log> pipeline = pipelineBuilder.create(
      new LocalFileRepository(),
      file.getPath(),
      new JSONLogFactory(),
      List.of(new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor())
    );
    Batchable<Log> batcher = new LogBatcher(pipeline, 100);

    // when
    MessageBatch<Log> batch = batcher.nextBatch();

    // then
    assertThat(batch.get()).isEqualTo(Collections.EMPTY_LIST);

  }

  @DisplayName("Should get batch according to the Batcher batch size")
  @Test
  void nextBatchTest() {
    File file = Path.of("src/test/resources/sample-data/empty-included.ndjson").toFile();

    PipelineBuilder<Log> pipelineBuilder = new FileBaseLogPipelineBuilder();
    Pipeline<Log> pipeline = pipelineBuilder.create(
      new LocalFileRepository(),
      file.getPath(),
      new JSONLogFactory(),
      List.of(new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor())
    );
    Batchable<Log> batcher = new LogBatcher(pipeline, 3);

    // when
    MessageBatch<Log> batch = batcher.nextBatch();

    // then
    assertThat(batch.get()).hasSize(3);
  }
}
