package sourceconnector.repository.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class S3FileRepositoryTest extends S3TestSupport {

  @DisplayName("Should get InputStream when the file exists")
  @Test
  void getFileTest() {
    // given
    Path file = Path.of("src/test/resources/sample-data/empty.ndjson");
    String s3Path = "test-path/empty.ndjson";
    this.upload(s3Path, file);

    S3FileRepository fileRepository = new S3FileRepository(
      s3Client,
      BUCKET_NAME
    );

    // when
    InputStream inputStream = fileRepository.getFile(s3Path);

    // then
    assertThat(inputStream).isNotNull();
  }

  @DisplayName("Should throw NotFoundException when the file does not exist")
  @Test
  void notFoundFileTest() {
    // given
    String s3Path = "test-path/not-exist-file.ndjson";

    S3FileRepository fileRepository = new S3FileRepository(
      s3Client,
      BUCKET_NAME
    );

    // when then
    assertThatThrownBy(() -> fileRepository.getFile(s3Path))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Failed to get file from S3: " + s3Path);
  }
}
