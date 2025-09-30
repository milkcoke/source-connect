package sourceconnector.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.FileRepository;
import sourceconnector.repository.file.LocalFileRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileRepositoryTest {

  private final FileRepository fileRepository = new LocalFileRepository();
  @DisplayName("Should throw not found remoteoffsetmanager.exception when file does not exist")
  @Test
  void shouldThrowNoSuchFileException() {
    Assertions.assertThatThrownBy(()->fileRepository.getFile("not-exist-file.txt"))
      .isInstanceOf(NoSuchFileException.class)
      .hasMessage("not-exist-file.txt");
  }

  @DisplayName("Should get inpustream when file exists")
  @Test
  void getInputStream() throws IOException {
    // given
    File file = Path.of("src/test/resources/test-file.txt").toFile();
    file.createNewFile();
    // when
    InputStream inputStream = this.fileRepository.getFile(file.getPath());
    // then
    assertThat(inputStream).isNotNull();
    file.delete();
  }

  @DisplayName("Should get inpustream when file exists in local file system")
  @Test
  void getDownloadDirectoryFileInputStream() throws IOException {
    // given
    File sampleFile = Paths.get(System.getProperty("user.home"), "Downloads", "sample.txt").toFile();
    // when
    sampleFile.createNewFile();
    InputStream inputStream = this.fileRepository.getFile(sampleFile.getPath());
    // then
    sampleFile.delete();
  }
}
