package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.IOException
import java.nio.file.Path

internal class DecompressingFileRepositoryTest {
  @DisplayName("Should get inputstream of decompressed local file")
  @Test
  @Throws(IOException::class)
  fun getLocalFileDecompressed() {
    // given
    val compressedPath = Path.of("src/test/resources/sample-data/compressed/empty-included.zip")
    val originalPath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val zipFileKey: FileKey = from(compressedPath)
    val plainFileKey: FileKey = from(originalPath)

    val localFileRepository: FileRepository = LocalFileRepository()
    val decompressingFileRepository: FileRepository = DecompressingFileRepository(localFileRepository)

    decompressingFileRepository.getFile(zipFileKey).use { decompressed ->
      localFileRepository.getFile(plainFileKey).use { plainInputStream ->
        val decompressedBytes = decompressed.readAllBytes()
        val plainBytes = plainInputStream.readAllBytes()
        // then
        Assertions.assertThat(decompressedBytes).isEqualTo(plainBytes)
      }
    }
  }
}
