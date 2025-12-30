package sourceconnector.repository.file.decompressor

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.repository.file.FileRepository
import sourceconnector.repository.file.LocalFileRepository
import java.io.IOException
import java.nio.file.Path

internal class ZipDecompressorTest {
  @DisplayName("Should be true only when file key ends with .zip")
  @Test
  fun supports() {
    // given
    val fileKey: FileKey = from(Path.of("file.ndjson.zip"))
    val zipDecompressor: Decompressor = ZipDecompressor()

    // when
    val result = zipDecompressor.supports(fileKey)

    // then
    assertThat(result).isTrue()
  }

  @DisplayName("Should be false when file key does not end with .zip")
  @Test
  fun notSupportedFormatTest() {
    // given
    val fileKey: FileKey = from(Path.of("file.ndjson"))
    val zipDecompressor: Decompressor = ZipDecompressor()

    // when
    val result = zipDecompressor.supports(fileKey)

    // then
    assertThat(result).isFalse()
  }

  @DisplayName("Should decompress zip input stream correctly")
  @Test
  @Throws(IOException::class)
  fun decompress() {
    // given
    val zipPath = Path.of("src/test/resources/sample-data/compressed/empty-included.zip")
    val plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val gzipFileKey: FileKey = from(zipPath)
    val plainFileKey: FileKey = from(plainPath)
    val zipDecompressor: Decompressor = ZipDecompressor()
    val fileRepository: FileRepository = LocalFileRepository()

    zipDecompressor.decompress(fileRepository.getFile(gzipFileKey)).use { decompressed ->
      fileRepository.getFile(plainFileKey).use { plain ->
        val decompressedBytes = decompressed.readAllBytes()
        val plainBytes = plain.readAllBytes()
        // then
        assertThat(decompressedBytes).isEqualTo(plainBytes)
      }
    }
  }

  @DisplayName("Should throw Exception when decompressing invalid zip stream")
  @Test
  fun decompressFailTest() {
    val plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val plainFileKey: FileKey = from(plainPath)
    val zipDecompressor: Decompressor = ZipDecompressor()
    val fileRepository: FileRepository = LocalFileRepository()

    // when
    Assertions.assertThatThrownBy {
      zipDecompressor.decompress(
        fileRepository.getFile(
          plainFileKey
        )
      )
    }
      .isInstanceOf(IOException::class.java)
      .hasMessage("Empty zip file")
  }
}
