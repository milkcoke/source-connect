package sourceconnector.repository.file;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;

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
    FileKey fileKey = LocalFileKey.from(Path.of("not-exist-file.txt"));
    Assertions.assertThatThrownBy(()->fileRepository.getFile(fileKey))
      .isInstanceOf(NoSuchFileException.class)
      .hasMessageContaining("not-exist-file.txt");
  }

  @DisplayName("Should get inpustream when file exists")
  @Test
  void getInputStream() throws IOException {
    // given
    Path path = Path.of("src/test/resources/test-file.txt");
    File file = path.toFile();
    file.createNewFile();
    // when
    FileKey fileKey = LocalFileKey.from(path);
    InputStream inputStream = this.fileRepository.getFile(fileKey);
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
    FileKey fileKey = LocalFileKey.from(sampleFile.toPath());
    InputStream inputStream = this.fileRepository.getFile(fileKey);
    // then
    sampleFile.delete();
  }
}
