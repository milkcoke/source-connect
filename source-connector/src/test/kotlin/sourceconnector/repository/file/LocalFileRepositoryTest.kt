package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

internal class LocalFileRepositoryTest {
  private val fileRepository: FileRepository = LocalFileRepository()

  @TempDir
  private lateinit var tempDir: Path

  @DisplayName("Should throw not found remoteoffsetmanager.exception when file does not exist")
  @Test
  fun shouldThrowNoSuchFileException() {
    val fileKey: FileKey = from(Path.of("not-exist-file.txt"))
    Assertions.assertThatThrownBy { fileRepository.getFile(fileKey) }
      .isInstanceOf(NoSuchFileException::class.java)
      .hasMessageContaining("not-exist-file.txt")
  }

  @DisplayName("Should get inpustream when file exists")
  @Test
  @Throws(IOException::class)
  fun getInputStream() {
    // given
    val tempFilePath = Files.createTempFile(tempDir, null, null)
    // when
    val fileKey: FileKey = from(tempFilePath)
    val inputStream = this.fileRepository.getFile(fileKey)
    // then
    assertThat(inputStream).isNotNull()
  }

  @DisplayName("Should get inpustream when file exists in local file system")
  @Test
  @Throws(IOException::class)
  fun getDownloadDirectoryFileInputStream() {
    // given
    val tempFilePath = Files.createTempFile(tempDir, null, null)
    // when
    val fileKey: FileKey = from(tempFilePath)
    // then
    val inputStream = this.fileRepository.getFile(fileKey)
    assertThat(inputStream).isNotNull()
  }
}
