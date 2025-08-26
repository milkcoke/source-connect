package sourceconnector.repository;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LocalFileRepositoryTest {

  private final FileRepository fileRepository = new LocalFileRepository();
  @DisplayName("Should throw not found exception when file does not exist")
  @Test
  void shouldThrowNoSuchFileException() {
    Assertions.assertThatThrownBy(()->fileRepository.getFile("not-exist-file.txt"))
      .isInstanceOf(NoSuchFileException.class)
      .hasMessage("not-exist-file.txt");
  }

  @SneakyThrows(IOException.class)
  @DisplayName("Should get inpustream when file exists")
  @Test
  void getInputStream() {
    // given
    File file = Path.of("src/test/resources/test-file.txt").toFile();
    file.createNewFile();
    // when
    InputStream inputStream = this.fileRepository.getFile(file.getPath());
    // then
    assertThat(inputStream).isNotNull();
    file.delete();
  }

  @SneakyThrows(IOException.class)
  @DisplayName("Should get inpustream when file exists in local file system")
  @Test
  void getDownloadDirectoryFileInputStream() {
    // given
    File sampleFile = Paths.get(System.getProperty("user.home"), "Downloads", "sample.txt").toFile();
    // when
    sampleFile.createNewFile();
    InputStream inputStream = this.fileRepository.getFile(sampleFile.getPath());
    // then
    sampleFile.delete();
  }
}
