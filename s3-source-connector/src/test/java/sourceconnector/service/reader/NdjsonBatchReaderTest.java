package sourceconnector.service.reader;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.FileRepository;
import sourceconnector.repository.LocalFileRepository;
import sourceconnector.service.processor.BatchProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NdjsonBatchReaderTest {

  private final FileRepository fileRepository = new LocalFileRepository();

  @SneakyThrows(IOException.class)
  @DisplayName("Should read next batch of records according to the batch size")
  @Test
  void nextBatch() {
    // given
    InputStream inputStream = fileRepository.getFile("src/test/resources/sample-data/large-ndjson.ndjson");
    BatchReader<String> batchReader = new NdjsonBatchReader(inputStream, 100);

    // when
    List<String> batch = batchReader.nextBatch();
    // then
    assertThat(batch).hasSize(100);
  }
}
