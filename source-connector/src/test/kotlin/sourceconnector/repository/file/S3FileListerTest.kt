package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.repository.file.validator.NoConditionFileValidator
import sourceconnector.support.S3TestSupport
import java.io.IOException
import java.nio.file.Path

internal class S3FileListerTest : S3TestSupport() {
  @DisplayName("Should get empty list when no files exist in the path")
  @Test
  @Throws(IOException::class)
  fun listFilesNotExistPath() {
    // given
    val fileLister: FileLister = S3FileLister(
      s3Client,
      NoConditionFileValidator()
    )
    val s3FileKey = S3Location(BUCKET_NAME, "not-exist-path.txt").toFileKey()
    // when
    val fileKeys: List<FileKey> = fileLister.listFilesRecursively(s3FileKey)
    // then
    assertThat<FileKey>(fileKeys).isEmpty()
  }

  @DisplayName("Should get file list with no recursive")
  @Test
  @Throws(IOException::class)
  suspend fun getListWithoutRecursiveTest() {
    // given
    val sampleFilNames: List<String> = listOf(
      "empty.ndjson",
      "empty-included.ndjson",
      "subdir1/sub1.ndjson",
      "subdir1/sub2.csv"
    )
    this.uploadSamples(sampleFilNames)

    val fileLister: FileLister = S3FileLister(
      s3Client,
      NoConditionFileValidator()
    )

    val fileKey = S3Location(BUCKET_NAME, "resources/sample-data/").toFileKey()
    // when
    val filePaths = fileLister.listFiles(fileKey)
      .stream()
      .map<String> { obj: FileKey? -> obj!!.get() }
      .toList()

    // then
    assertThat<String?>(filePaths).hasSize(2)
      .containsExactlyInAnyOrder(
        "s3://test-bucket/resources/sample-data/empty.ndjson",
        "s3://test-bucket/resources/sample-data/empty-included.ndjson"
      )
  }

  @DisplayName("Should get file list with recursive")
  @Test
  @Throws(IOException::class)
  suspend fun getListWithRecursive() {
    // given
    val sampleFilNames: List<String> = listOf(
      "empty.ndjson",
      "empty-included.ndjson",
      "subdir1/sub1.ndjson",
      "subdir1/sub2.csv"
    )

    this.uploadSamples(sampleFilNames)
    val fileLister: FileLister = S3FileLister(
      s3Client,
      NoConditionFileValidator()
    )

    val fileKey = S3Location(BUCKET_NAME, "resources/sample-data/").toFileKey()
    // when
    val filePaths = fileLister.listFilesRecursively(fileKey)
      .stream()
      .map<String> { obj: FileKey? -> obj!!.get() }
      .toList()

    // then
    assertThat<String?>(filePaths).hasSize(4)
      .containsExactlyInAnyOrder(
        "s3://test-bucket/resources/sample-data/empty.ndjson",
        "s3://test-bucket/resources/sample-data/empty-included.ndjson",
        "s3://test-bucket/resources/sample-data/subdir1/sub1.ndjson",
        "s3://test-bucket/resources/sample-data/subdir1/sub2.csv"
      )
  }

  private suspend fun uploadSamples(fileNames: List<String>) {
    for (file in fileNames) {
      val localPath = Path.of("src/test/resources/sample-data/", file)
      val s3Location = S3Location(
        bucket = BUCKET_NAME,
        key = "resources/sample-data/$file"
      )
      this.upload(s3Location, localPath)
    }
  }
}
