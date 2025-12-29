package offsetmanager.domain.file

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class S3FileKeyTest {
  @DisplayName("Should equal when having same s3 path")
  @Test
  fun s3KeyEqualityTest() {
    // given
    val fileKey1: FileKey = S3Uri.from("s3://test-bucket/file1.txt").toFileKey()
    val fileKey2: FileKey = S3Uri.from("s3://test-bucket/file1.txt").toFileKey()
    // when then
    assertThat<FileKey>(fileKey1).isEqualTo(fileKey2)
  }

  @DisplayName("Should not equal when having different s3Path Path")
  @Test
  fun s3KeyNotEqualityTest() {
    // given
    val fileKey1: FileKey = S3Uri.from("s3://test-bucket/file1.txt").toFileKey()
    val fileKey2: FileKey = S3Uri.from("s3://test-bucket/file2.txt").toFileKey()
    // when then
    assertThat(fileKey1).isNotEqualTo(fileKey2)
  }
}
