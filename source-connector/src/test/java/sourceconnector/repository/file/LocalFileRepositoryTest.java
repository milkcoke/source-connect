package sourceconnector.repository.file;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileRepositoryTest {

  private final FileRepository fileRepository = new LocalFileRepository();

  @TempDir
  Path tempDir;

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
    Path tempFilePath = Files.createTempFile(tempDir, null, null);
    // when
    FileKey fileKey = LocalFileKey.from(tempFilePath);
    InputStream inputStream = this.fileRepository.getFile(fileKey);
    // then
    assertThat(inputStream).isNotNull();
  }

  @DisplayName("Should get inpustream when file exists in local file system")
  @Test
  void getDownloadDirectoryFileInputStream() throws IOException {
    // given
    Path tempFilePath = Files.createTempFile(tempDir, null, null);
    // when
    FileKey fileKey = LocalFileKey.from(tempFilePath);
    // then
    InputStream inputStream = this.fileRepository.getFile(fileKey);
    assertThat(inputStream).isNotNull();
  }
}
