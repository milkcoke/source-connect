package sourceconnector.repository.file.decompressor

import com.github.luben.zstd.ZstdIOException
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

internal class ZstdDecompressorTest {
  @DisplayName("Should be true only when file key ends with .zst")
  @Test
  fun supports() {
    // given
    val fileKey: FileKey = from(Path.of("file.ndjson.zst"))
    val zstdDecompressor: Decompressor = ZstdDecompressor()

    // when
    val result = zstdDecompressor.supports(fileKey)

    // then
    assertThat(result).isTrue()
  }

  @DisplayName("Should be false when file key does not end with .zst")
  @Test
  fun notSupportedFormatTest() {
    // given
    val fileKey: FileKey = from(Path.of("file.ndjson"))
    val zstdDecompressor: Decompressor = ZstdDecompressor()

    // when
    val result = zstdDecompressor.supports(fileKey)

    // then
    assertThat(result).isFalse()
  }

  @DisplayName("Should decompress zip input stream correctly")
  @Test
  @Throws(IOException::class)
  fun decompress() {
    // given
    val zstdPath = Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.zst")
    val plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val gzipFileKey: FileKey = from(zstdPath)
    val plainFileKey: FileKey = from(plainPath)
    val zstdDecompressor: Decompressor = ZstdDecompressor()
    val fileRepository: FileRepository = LocalFileRepository()

    zstdDecompressor.decompress(fileRepository.getFile(gzipFileKey)).use { decompressed ->
      fileRepository.getFile(plainFileKey).use { plain ->
        val decompressedBytes = decompressed.readAllBytes()
        val plainBytes = plain.readAllBytes()
        // then
        assertThat(decompressedBytes).isEqualTo(plainBytes)
      }
    }
  }

  @DisplayName("Should throw Exception when decompressing invalid zstd stream")
  @Test
  @Throws(IOException::class)
  fun decompressFailTest() {
    val plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val plainFileKey: FileKey = from(plainPath)
    val zstdDecompressor: Decompressor = ZstdDecompressor()
    val fileRepository: FileRepository = LocalFileRepository()

    // when
    val inputStream = zstdDecompressor.decompress(fileRepository.getFile(plainFileKey))
    // then
    Assertions.assertThatThrownBy { inputStream.read() }
      .isInstanceOf(ZstdIOException::class.java)
  }
}
