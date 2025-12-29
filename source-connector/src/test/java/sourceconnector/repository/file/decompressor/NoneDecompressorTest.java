package sourceconnector.repository.file.decompressor;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import sourceconnector.repository.file.FileRepository;
import sourceconnector.repository.file.LocalFileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NoneDecompressorTest {

  private final Decompressor noneDecompressor = new NoneDecompressor();

  @DisplayName("Always be true regardless of file key")
  @ParameterizedTest(name = "supports({0}) should return true")
  @ValueSource(strings = {
    "file.ndjson",
    "file.ndjson.gz",
    "file.ndjson.zip",
    "file.ndjson.zst",
    "file.txt",
    "file.csv",
    "file"
  })
  void supports(String fileName) {
    // given
    FileKey fileKey = LocalFileKey.from(Path.of(fileName));
    // when
    boolean result = noneDecompressor.supports(fileKey);

    // then
    assertThat(result).isTrue();
  }

  @DisplayName("Should bypass decompression regardless of input stream")
  @ParameterizedTest(name = "Bypass decompression for {0}")
  @MethodSource("files")
  void decompress(Path path) throws IOException {
    // given
    FileKey fileKey = LocalFileKey.from(path);
    FileRepository fileRepository = new LocalFileRepository();

    // when
    try (
      InputStream decompressed = noneDecompressor.decompress(fileRepository.getFile(fileKey));
      InputStream plain = fileRepository.getFile(fileKey);
    ) {
      byte[] decompressedBytes = decompressed.readAllBytes();
      byte[] originalBytes = plain.readAllBytes();
      // then
      assertThat(decompressedBytes).isEqualTo(originalBytes);
    }
  }

  static Stream<Path> files() {
    return Stream.of(
      Path.of("src/test/resources/sample-data/empty-included.ndjson"),
      Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.gz"),
      Path.of("src/test/resources/sample-data/compressed/empty-included.zip"),
      Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.zst")
    );
  }
}
