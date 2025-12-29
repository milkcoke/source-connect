package sourceconnector.repository.file.decompressor;

import com.github.luben.zstd.ZstdIOException;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.FileRepository;
import sourceconnector.repository.file.LocalFileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZstdDecompressorTest {

  @DisplayName("Should be true only when file key ends with .zst")
  @Test
  void supports() {
    // given
    FileKey fileKey = LocalFileKey.from(Path.of("file.ndjson.zst"));
    Decompressor zstdDecompressor = new ZstdDecompressor();

    // when
    boolean result = zstdDecompressor.supports(fileKey);

    // then
    assertThat(result).isTrue();
  }

  @DisplayName("Should be false when file key does not end with .zst")
  @Test
  void notSupportedFormatTest() {
    // given
    FileKey fileKey = LocalFileKey.from(Path.of("file.ndjson"));
    Decompressor zstdDecompressor = new ZstdDecompressor();

    // when
    boolean result = zstdDecompressor.supports(fileKey);

    // then
    assertThat(result).isFalse();
  }

  @DisplayName("Should decompress zip input stream correctly")
  @Test
  void decompress() throws IOException {
    // given
    Path zstdPath = Path.of("src/test/resources/sample-data/compressed/empty-included.ndjson.zst");
    Path plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson");
    FileKey gzipFileKey = LocalFileKey.from(zstdPath);
    FileKey plainFileKey = LocalFileKey.from(plainPath);
    Decompressor zstdDecompressor = new ZstdDecompressor();
    FileRepository fileRepository = new LocalFileRepository();

    // when
    try (
      InputStream decompressed = zstdDecompressor.decompress(fileRepository.getFile(gzipFileKey));
      InputStream plain = fileRepository.getFile(plainFileKey);
    ) {
      byte[] decompressedBytes = decompressed.readAllBytes();
      byte[] plainBytes = plain.readAllBytes();
      // then
      assertThat(decompressedBytes).isEqualTo(plainBytes);
    }
  }

  @DisplayName("Should throw Exception when decompressing invalid zstd stream")
  @Test
  void decompressFailTest() throws IOException {
    Path plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson");
    FileKey plainFileKey = LocalFileKey.from(plainPath);
    Decompressor zstdDecompressor = new ZstdDecompressor();
    FileRepository fileRepository = new LocalFileRepository();

    // when
    InputStream inputStream =  zstdDecompressor.decompress(fileRepository.getFile(plainFileKey));
    // then
    assertThatThrownBy(inputStream::read)
      .isInstanceOf(ZstdIOException.class);
  }
}
