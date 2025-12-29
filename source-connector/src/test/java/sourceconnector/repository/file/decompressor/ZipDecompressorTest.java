package sourceconnector.repository.file.decompressor;

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

class ZipDecompressorTest {

  @DisplayName("Should be true only when file key ends with .zip")
  @Test
  void supports() {
    // given
    FileKey fileKey = LocalFileKey.from(Path.of("file.ndjson.zip"));
    Decompressor zipDecompressor = new ZipDecompressor();

    // when
    boolean result = zipDecompressor.supports(fileKey);

    // then
    assertThat(result).isTrue();
  }

  @DisplayName("Should be false when file key does not end with .zip")
  @Test
  void notSupportedFormatTest() {
    // given
    FileKey fileKey = LocalFileKey.from(Path.of("file.ndjson"));
    Decompressor zipDecompressor = new ZipDecompressor();

    // when
    boolean result = zipDecompressor.supports(fileKey);

    // then
    assertThat(result).isFalse();
  }

  @DisplayName("Should decompress zip input stream correctly")
  @Test
  void decompress() throws IOException {
    // given
    Path zipPath = Path.of("src/test/resources/sample-data/compressed/empty-included.zip");
    Path plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson");
    FileKey gzipFileKey = LocalFileKey.from(zipPath);
    FileKey plainFileKey = LocalFileKey.from(plainPath);
    Decompressor zipDecompressor = new ZipDecompressor();
    FileRepository fileRepository = new LocalFileRepository();

    // when
    try (
      InputStream decompressed = zipDecompressor.decompress(fileRepository.getFile(gzipFileKey));
      InputStream plain = fileRepository.getFile(plainFileKey);
    ) {
      byte[] decompressedBytes = decompressed.readAllBytes();
      byte[] plainBytes = plain.readAllBytes();
      // then
      assertThat(decompressedBytes).isEqualTo(plainBytes);
    }
  }

  @DisplayName("Should throw Exception when decompressing invalid zip stream")
  @Test
  void decompressFailTest() {
    Path plainPath = Path.of("src/test/resources/sample-data/empty-included.ndjson");
    FileKey plainFileKey = LocalFileKey.from(plainPath);
    Decompressor zipDecompressor = new ZipDecompressor();
    FileRepository fileRepository = new LocalFileRepository();

    // when
    assertThatThrownBy(() -> zipDecompressor.decompress(fileRepository.getFile(plainFileKey)))
      .isInstanceOf(IOException.class)
      .hasMessage("Empty zip file");
  }
}
