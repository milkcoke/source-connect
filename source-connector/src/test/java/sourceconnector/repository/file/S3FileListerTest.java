package sourceconnector.repository.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.validator.NoConditionFileValidator;

import java.io.IOException;
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
    List<String> filePaths = fileLister.listFiles(true, "not-exist-path");
    // then
    assertThat(filePaths).isEmpty();
  }

  //TODO: exist file paths with no recursive
  //   and with recursive

}
