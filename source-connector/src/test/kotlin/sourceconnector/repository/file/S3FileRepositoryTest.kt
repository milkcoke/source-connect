package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.S3Uri.Companion.of
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.support.S3TestSupport
import java.nio.file.Path

internal class S3FileRepositoryTest : S3TestSupport() {
  @DisplayName("Should get InputStream when the file exists")
  @Test
   fun getFileTest() {
    // given
    val localFilePath = Path.of("src/test/resources/sample-data/empty-included.ndjson")
    val s3Location = S3Location(BUCKET_NAME, "sample-data/empty-included.ndjson")
    this.upload(s3Location, localFilePath)

    val fileRepository = S3FileRepository(s3Client)

    // when
    val inputStream = fileRepository.getFile(s3Location.toFileKey())

    // then
    assertThat(inputStream).isNotNull()
  }

  @DisplayName("Should throw NotFoundException when the file does not exist")
  @Test
  fun notFoundFileTest() {
    // given
    val fileKey: FileKey = of(BUCKET_NAME, "not-exist-file.csv").toFileKey()

    val fileRepository = S3FileRepository(s3Client)

    // when then
    Assertions.assertThatThrownBy { fileRepository.getFile(fileKey) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("Failed to get file from: " + fileKey.get())
  }
}
