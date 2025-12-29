package sourceconnector.repository.file;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DecompressingFileRepositoryTest {

  @DisplayName("Should get inputstream of decompressed local file")
  @Test
  void getLocalFileDecompressed() throws IOException {
    // given
    Path compressedPath = Path.of("src/test/resources/sample-data/compressed/empty-included.zip");
    Path originalPath = Path.of("src/test/resources/sample-data/empty-included.ndjson");
    FileKey zipFileKey = LocalFileKey.from(compressedPath);
    FileKey plainFileKey = LocalFileKey.from(originalPath);

    FileRepository localFileRepository = new LocalFileRepository();
    FileRepository decompressingFileRepository = new DecompressingFileRepository(localFileRepository);

    // when
    try (
      InputStream decompressed = decompressingFileRepository.getFile(zipFileKey);
      InputStream plainInputStream = localFileRepository.getFile(plainFileKey);
    ) {
      byte[] decompressedBytes = decompressed.readAllBytes();
      byte[] plainBytes = plainInputStream.readAllBytes();
      // then
      assertThat(decompressedBytes).isEqualTo(plainBytes);
    }
  }


}
