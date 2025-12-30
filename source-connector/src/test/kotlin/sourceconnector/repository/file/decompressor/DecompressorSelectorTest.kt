package sourceconnector.repository.file.decompressor

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import sourceconnector.repository.file.decompressor.DecompressorSelector.Companion.select
import java.nio.file.Path
import java.util.stream.Stream

internal class DecompressorSelectorTest {
  @DisplayName("Should select appropriate decompressor based on file key")
  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("fileKeys")
  fun select(fileKey: FileKey, expected: Decompressor) {
    // when
    val actualDecompressor = select(fileKey)

    // then
    assertThat<Decompressor>(actualDecompressor).isInstanceOf(expected.javaClass)
  }

  companion object {
    // given
    @JvmStatic
    fun fileKeys(): Stream<Arguments?> {
      return Stream.of<Arguments?>(
        Arguments.of(
          Named.named<LocalFileKey>(
            ".gz file",
            from(Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.gz"))
          ),
          Named.named<GzipDecompressor?>("GzipDecompressor", GzipDecompressor())
        ),
        Arguments.of(
          Named.named<LocalFileKey>(
            ".zip file",
            from(Path.of("src/test/resources/sample-data/compressed/empty-included.zip"))
          ),
          Named.named<ZipDecompressor?>("ZipDecompressor", ZipDecompressor())
        ),
        Arguments.of(
          Named.named<LocalFileKey>(
            ".zstd file",
            from(Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.zst"))
          ),
          Named.named<ZstdDecompressor?>("ZstdDecompressor", ZstdDecompressor())
        ),
        Arguments.of(
          Named.named<LocalFileKey>(
            "others file",
            from(Path.of("src/test/resources/sample-data/empty-included.ndjson"))
          ),
          Named.named<NoneDecompressor?>("NoneDecompressor", NoneDecompressor())
        )
      )
    }
  }
}
