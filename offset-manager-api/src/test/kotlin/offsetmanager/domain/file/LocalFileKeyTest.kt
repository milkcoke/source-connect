package offsetmanager.domain.file

import jdk.jfr.Description
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class LocalFileKeyTest {
  @DisplayName("Should equal when having same file Path")
  @Test
  fun pathEqualityTest() {
    // given
    val fileKey1: FileKey = from(Path.of("file1.txt"))
    val fileKey2: FileKey = from(Path.of("file1.txt"))
    // when then
    Assertions.assertThat<FileKey?>(fileKey1).isEqualTo(fileKey2)
  }

  @DisplayName("Should not equal when having different file Path")
  @Test
  fun pathNotEqualityTest() {
    // given
    val fileKey1: FileKey = from(Path.of("file1.txt"))
    val fileKey2: FileKey = from(Path.of("file2.txt"))
    // when then
    Assertions.assertThat<FileKey?>(fileKey1).isNotEqualTo(fileKey2)
  }


  @TempDir
  lateinit var tempDir: Path

  @Description("Testing file path independent of OS")
  @DisplayName("LocalKey has always file:/// prefix")
  @Test
  @Throws(IOException::class)
  fun linuxMacStyleTest() {
    // given
    val tempFilePath = Files.createTempFile(tempDir, null, null)

    // when
    val fileKey: FileKey = from(tempFilePath)

    // then
    Assertions.assertThat(fileKey.get()).startsWith("file:///")
  }
}
