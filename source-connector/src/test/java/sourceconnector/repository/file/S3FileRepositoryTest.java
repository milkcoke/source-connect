package sourceconnector.repository.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.file.FileKey;
import sourceconnector.domain.file.S3Uri;

import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3FileRepositoryTest extends S3TestSupport {

  @DisplayName("Should get InputStream when the file exists")
  @Test
  void getFileTest() {
    // given
    Path localFilePath = Path.of("src/test/resources/sample-data/empty.ndjson");
    S3Location s3Location = new S3Location(BUCKET_NAME, "sample-data/empty.ndjson");
    this.upload(s3Location, localFilePath);

    S3FileRepository fileRepository = new S3FileRepository(s3Client);

    // when
    InputStream inputStream = fileRepository.getFile(s3Location.toFileKey());

    // then
    assertThat(inputStream).isNotNull();
  }

  @DisplayName("Should throw NotFoundException when the file does not exist")
  @Test
  void notFoundFileTest() {
    // given
    FileKey fileKey = S3Uri.of(BUCKET_NAME, "not-exist-file.csv").toFileKey();

    S3FileRepository fileRepository = new S3FileRepository(s3Client);

    // when then
    assertThatThrownBy(() -> fileRepository.getFile(fileKey))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Failed to get file from: " + fileKey.get());
  }
}
