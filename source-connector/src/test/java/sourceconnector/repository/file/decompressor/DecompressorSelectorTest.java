package sourceconnector.repository.file.decompressor;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;

class DecompressorSelectorTest {

  // given
  static Stream<Arguments> fileKeys() {
    return Stream.of(
      Arguments.of(
        named(".gz file", LocalFileKey.from(Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.gz"))),
        named("GzipDecompressor", new GzipDecompressor())
      ),
      Arguments.of(
        named(".zip file", LocalFileKey.from(Path.of("src/test/resources/sample-data/compressed/empty-included.zip"))),
        named("ZipDecompressor", new ZipDecompressor())
      ),
      Arguments.of(
        named(".zstd file", LocalFileKey.from(Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.zst"))),
        named("ZstdDecompressor", new ZstdDecompressor())
      ),
      Arguments.of(
        named("others file", LocalFileKey.from(Path.of("src/test/resources/sample-data/empty-included.ndjson"))),
        named("NoneDecompressor", new NoneDecompressor())
      )
    );
  }


  @DisplayName("Should select appropriate decompressor based on file key")
  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("fileKeys")
  void select(FileKey fileKey, Decompressor expected) {
    // when
    Decompressor actualDecompressor = DecompressorSelector.select(fileKey);

    // then
    assertThat(actualDecompressor).isInstanceOf(expected.getClass());
  }
}
