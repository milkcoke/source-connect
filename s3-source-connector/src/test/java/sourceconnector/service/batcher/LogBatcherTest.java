package sourceconnector.service.batcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.batch.MessageBatch;
import sourceconnector.domain.factory.JSONLogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.repository.LocalFileRepository;
import sourceconnector.service.pipeline.FileBaseLogPipeline;
import sourceconnector.service.pipeline.Pipeline;
import sourceconnector.service.processor.impl.EmptyFilterProcessor;
import sourceconnector.service.processor.impl.TrimMapperProcessor;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class LogBatcherTest {

  @DisplayName("Should return emptyList when no more logs to batch")
  @Test
  void nextBatchAtEmptyFile() {
    // given
    File file = Path.of("src/test/resources/sample-data/empty.ndjson").toFile();

    Pipeline<Log> pipeline = FileBaseLogPipeline.create(
      new LocalFileRepository(),
      file.getPath(),
      new JSONLogFactory(),
      new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor()
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

    Pipeline<Log> pipeline = FileBaseLogPipeline.create(
      new LocalFileRepository(),
      file.getPath(),
      new JSONLogFactory(),
      new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor()
    );
    Batchable<Log> batcher = new LogBatcher(pipeline, 3);

    // when
    MessageBatch<Log> batch = batcher.nextBatch();

    // then
    assertThat(batch.get()).hasSize(3);
  }
}
