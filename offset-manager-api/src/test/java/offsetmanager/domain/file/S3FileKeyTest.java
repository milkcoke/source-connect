package offsetmanager.domain.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class S3FileKeyTest {

  @DisplayName("Should equal when having same s3 path")
  @Test
  void s3KeyEqualityTest() {
    // given
    FileKey fileKey1 = S3Uri.from("s3://test-bucket/file1.txt").toFileKey();
    FileKey fileKey2 = S3Uri.from("s3://test-bucket/file1.txt").toFileKey();
    // when then
    assertThat(fileKey1).isEqualTo(fileKey2);
  }

  @DisplayName("Should not equal when having different s3Path Path")
  @Test
  void s3KeyNotEqualityTest() {
    // given
    FileKey fileKey1 = S3Uri.from("s3://test-bucket/file1.txt").toFileKey();
    FileKey fileKey2 = S3Uri.from("s3://test-bucket/file2.txt").toFileKey();
    // when then
    assertThat(fileKey1).isNotEqualTo(fileKey2);
  }
}
