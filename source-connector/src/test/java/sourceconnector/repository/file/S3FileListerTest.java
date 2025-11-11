package sourceconnector.repository.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.validator.NoConditionFileValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class S3FileListerTest extends S3TestSupport {

  @DisplayName("Should get empty list when no files exist in the path")
  @Test
  void listFilesNotExistPath() throws IOException {
    // given
    FileLister fileLister = new S3FileLister(
      s3Client,
      BUCKET_NAME,
      new NoConditionFileValidator()
    );
    // when
    List<String> filePaths = fileLister.listFilesRecursively("not-exist-path");
    // then
    assertThat(filePaths).isEmpty();
  }

  @DisplayName("Should get file list with no recursive")
  @Test
  void getListWithoutRecursiveTest() throws IOException {
    // given
    List<String> sampleFilNames = List.of(
      "empty.ndjson",
      "empty-included.ndjson",
      "subdir1/sub1.ndjson",
      "subdir1/sub2.csv"
    );
    this.uploadSamples(sampleFilNames);

    FileLister fileLister = new S3FileLister(
      s3Client,
      BUCKET_NAME,
      new NoConditionFileValidator()
    );

    // when
    List<String> filePaths = fileLister.listFiles("resources/sample-data/");

    // then
    assertThat(filePaths).hasSize(2)
      .containsExactlyInAnyOrder(
        "s3://test-bucket/resources/sample-data/empty.ndjson",
        "s3://test-bucket/resources/sample-data/empty-included.ndjson"
      );
  }

  @DisplayName("Should get file list with recursive")
  @Test
  void getListWithRecursive() throws IOException {
    // given
    List<String> sampleFilNames = List.of(
      "empty.ndjson",
      "empty-included.ndjson",
      "subdir1/sub1.ndjson",
      "subdir1/sub2.csv"
    );

    this.uploadSamples(sampleFilNames);
    FileLister fileLister = new S3FileLister(
      s3Client,
      BUCKET_NAME,
      new NoConditionFileValidator()
    );

    // when
    List<String> filePaths = fileLister.listFilesRecursively( "resources/sample-data/");

    // then
    assertThat(filePaths).hasSize(4)
      .containsExactlyInAnyOrder(
        "s3://test-bucket/resources/sample-data/empty.ndjson",
        "s3://test-bucket/resources/sample-data/empty-included.ndjson",
        "s3://test-bucket/resources/sample-data/subdir1/sub1.ndjson",
        "s3://test-bucket/resources/sample-data/subdir1/sub2.csv"
      );
  }

  private void uploadSamples(List<String> fileNames) {

    for (String file : fileNames) {
      Path localPath = Path.of("src/test/resources/sample-data/", file);
      String s3Path = "resources/sample-data/" + file;
      this.upload(s3Path, localPath);
    }
  }


}
