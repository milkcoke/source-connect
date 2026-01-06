package sourceconnector.repository.file.decompressor

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.repository.file.FileRepository
import sourceconnector.repository.file.LocalFileRepository
import java.io.IOException
import java.nio.file.Path
import java.util.zip.ZipException

internal class GzipDecompressorTest {
  @DisplayName("Should be true only when file key ends with .gz")
  @Test
  fun supports() {
    // given
    val fileKey: FileKey = from(Path.of("file.ndjson.gz"))
    val gzipDecompressor = GzipDecompressor()

    // when
    val result = gzipDecompressor.supports(fileKey)

    // then
    assertThat(result).isTrue()
  }

  @DisplayName("Should be false when file key does not end with .gz")
  @Test
  fun notSupportedFormatTest() {
    // given
    val fileKey: FileKey = from(Path.of("file.ndjson"))
    val gzipDecompressor = GzipDecompressor()

    // when
    val result = gzipDecompressor.supports(fileKey)

    // then
    assertThat(result).isFalse()
  }

  @DisplayName("Should decompress gzip input stream correctly")
  @Test
  @Throws(IOException::class)
  fun decompress() {
    // given
    val gzipPath = Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.gz")
    val plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val gzipFileKey: FileKey = from(gzipPath)
    val plainFileKey: FileKey = from(plainPath)
    val gzipDecompressor = GzipDecompressor()
    val fileRepository: FileRepository = LocalFileRepository()

    gzipDecompressor.decompress(fileRepository.getFile(gzipFileKey)).use { decompressed ->
      fileRepository.getFile(plainFileKey).use { plain ->
        val decompressedBytes = decompressed.readAllBytes()
        val plainBytes = plain.readAllBytes()
        // then
        assertThat(decompressedBytes).isEqualTo(plainBytes)
      }
    }
  }

  @DisplayName("Should throw Exception when decompressing invalid gzip stream")
  @Test
  fun decompressFailTest() {
    val plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val plainFileKey: FileKey = from(plainPath)
    val gzipDecompressor = GzipDecompressor()
    val fileRepository: FileRepository = LocalFileRepository()

    // when
    Assertions.assertThatThrownBy {
      gzipDecompressor.decompress(
        fileRepository.getFile(
          plainFileKey
        )
      )
    }
      .isInstanceOf(ZipException::class.java)
      .hasMessage("Not in GZIP format")
  }
}
