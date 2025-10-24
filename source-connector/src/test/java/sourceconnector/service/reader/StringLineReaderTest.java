package sourceconnector.service.reader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.LocalFileRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class StringLineReaderTest {

  @DisplayName("Should return null when EOF")
  @Test
  void readAll() throws IOException {

    // given
    File file = Path.of("src/test/resources/sample-data/large.ndjson").toFile();
    InputStream inputStream = new LocalFileRepository().getFile(file.getPath());
    LineReader<String> reader = new StringLineReader(inputStream);

    // when
    String result;
    do {
      result = reader.read();
      if (reader.getLineNumber() == 90_000L) {
        System.out.println(result);
      }
    } while (result != null);

    assertThat(reader.getLineNumber()).isEqualTo(90_000L);
  }

  @DisplayName("Empty line is also counted")
  @Test
  void emptyLineTest() throws IOException {
    // given
    File file = Path.of("src/test/resources/sample-data/empty-included.ndjson").toFile();
    InputStream inputStream = new LocalFileRepository().getFile(file.getPath());
    LineReader<String> reader = new StringLineReader(inputStream);
    // when
    String result = null;
    int count = 0;
    while ((result = reader.read()) != null) {
      System.out.println(result);
      count++;
    }
    // then
    assertThat(reader.getLineNumber()).isEqualTo(17L);
    assertThat(count).isEqualTo(17);
  }

  @DisplayName("Start at the line number from 0 incremented whenever read()")
  @Test
  void startLineNumberTest() throws IOException {
    // given
    File file = Path.of("src/test/resources/sample-data/empty-included.ndjson").toFile();
    InputStream inputStream = new LocalFileRepository().getFile(file.getPath());
    LineReader<String> reader = new StringLineReader(inputStream);
    // when then
    assertThat(reader.getLineNumber()).isEqualTo(0L);
    reader.read();
    assertThat(reader.getLineNumber()).isEqualTo(1L);
    reader.read();
    assertThat(reader.getLineNumber()).isEqualTo(2L);
  }
}
