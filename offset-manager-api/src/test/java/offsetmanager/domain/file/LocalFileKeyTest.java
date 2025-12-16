package offsetmanager.domain.file;

import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileKeyTest {


  @DisplayName("Should equal when having same file Path")
  @Test
  void pathEqualityTest() {
    // given
    FileKey fileKey1 = LocalFileKey.from(Path.of("file1.txt"));
    FileKey fileKey2 = LocalFileKey.from(Path.of("file1.txt"));
    // when then
    assertThat(fileKey1).isEqualTo(fileKey2);
  }

  @DisplayName("Should not equal when having different file Path")
  @Test
  void pathNotEqualityTest() {
    // given
    FileKey fileKey1 = LocalFileKey.from(Path.of("file1.txt"));
    FileKey fileKey2 = LocalFileKey.from(Path.of("file2.txt"));
    // when then
    assertThat(fileKey1).isNotEqualTo(fileKey2);
  }


  @TempDir
  private Path tempDir;

  @Description("Testing file path independent of OS")
  @DisplayName("LocalKey has always file:/// prefix")
  @Test
  void linuxMacStyleTest() throws IOException {
    // given
    Path tempFilePath = Files.createTempFile(tempDir, null, null);

    // when
    FileKey fileKey = LocalFileKey.from(tempFilePath);

    // then
    assertThat(fileKey.get()).startsWith("file:///");
  }
}
