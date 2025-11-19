package sourceconnector.service.reader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import sourceconnector.repository.file.LocalFileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringLineReaderTest {

  @DisplayName("Should return null when EOF")
  @Test
  void readAll() throws IOException {
    // given
    Path localPath = Path.of("src/test/resources/sample-data/large.ndjson");
    FileKey fileKey = LocalFileKey.from(localPath);
    InputStream inputStream = new LocalFileRepository().getFile(fileKey);
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
    Path localPath = Path.of("src/test/resources/sample-data/empty-included.ndjson");
    FileKey fileKey = LocalFileKey.from(localPath);
    InputStream inputStream = new LocalFileRepository().getFile(fileKey);
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
    Path localPath = Path.of("src/test/resources/sample-data/empty-included.ndjson");
    FileKey fileKey = LocalFileKey.from(localPath);
    InputStream inputStream = new LocalFileRepository().getFile(fileKey);
    LineReader<String> reader = new StringLineReader(inputStream);
    // when then
    assertThat(reader.getLineNumber()).isEqualTo(0L);
    reader.read();
    assertThat(reader.getLineNumber()).isEqualTo(1L);
    reader.read();
    assertThat(reader.getLineNumber()).isEqualTo(2L);
  }

  @DisplayName("Start from the set line number")
  @Test
  void startFromSetLineNumberTest() throws IOException {
    // given
    Path localPath = Path.of("src/test/resources/sample-data/line-count.csv");
    FileKey fileKey = LocalFileKey.from(localPath);
    InputStream inputStream = new LocalFileRepository().getFile(fileKey);
    // when
    LineReader<String> reader = StringLineReader.withInitialLineNumber(inputStream, 5);
    // then
    assertThat(Integer.parseInt(reader.read())).isEqualTo(5);
  }

  @DisplayName("Should throw IllegalArgumentException when calling setLineNumber after read() called")
  @Test
  void failSetLineNumberTest() throws IOException {
    // given
    Path localPath = Path.of("src/test/resources/sample-data/line-count.csv");
    FileKey fileKey = LocalFileKey.from(localPath);
    InputStream inputStream = new LocalFileRepository().getFile(fileKey);
    // when then
    assertThatThrownBy(()->StringLineReader.withInitialLineNumber(inputStream, -100))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("initial line number must be greater than or equal to 0");
  }
}
