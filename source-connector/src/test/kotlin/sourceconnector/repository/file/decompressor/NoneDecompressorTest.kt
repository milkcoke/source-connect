package sourceconnector.repository.file.decompressor

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import sourceconnector.repository.file.FileRepository
import sourceconnector.repository.file.LocalFileRepository
import java.io.IOException
import java.nio.file.Path
import java.util.stream.Stream

internal class NoneDecompressorTest {
  private val noneDecompressor: Decompressor = NoneDecompressor()

  @DisplayName("Always be true regardless of file key")
  @ParameterizedTest(name = "supports({0}) should return true")
  @ValueSource(
    strings = ["file.ndjson", "file.ndjson.gz", "file.ndjson.zip", "file.ndjson.zst", "file.txt", "file.csv", "file"
    ]
  )
  fun supports(fileName: String) {
    // given
    val fileKey: FileKey = from(Path.of(fileName))
    // when
    val result = noneDecompressor.supports(fileKey)

    // then
    Assertions.assertThat(result).isTrue()
  }

  @DisplayName("Should bypass decompression regardless of input stream")
  @ParameterizedTest(name = "Bypass decompression for {0}")
  @MethodSource("files")
  @Throws(
    IOException::class
  )
  fun decompress(path: Path) {
    // given
    val fileKey: FileKey = from(path)
    val fileRepository: FileRepository = LocalFileRepository()

    noneDecompressor.decompress(fileRepository.getFile(fileKey)).use { decompressed ->
      fileRepository.getFile(fileKey).use { plain ->
        val decompressedBytes = decompressed.readAllBytes()
        val originalBytes = plain.readAllBytes()
        // then
        Assertions.assertThat(decompressedBytes).isEqualTo(originalBytes)
      }
    }
  }

  companion object {
    @JvmStatic
    fun files(): Stream<Path?> {
      return Stream.of<Path?>(
        Path.of("src/test/resources/sample-data/empty-included.ndjson"),
        Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.gz"),
        Path.of("src/test/resources/sample-data/compressed/empty-included.zip"),
        Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.zst")
      )
    }
  }
}
