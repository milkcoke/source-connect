package sourceconnector.repository.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.filter.FileExtensionFilter;
import sourceconnector.repository.file.validator.CompositeFileValidator;
import sourceconnector.repository.file.validator.FileValidator;
import sourceconnector.repository.file.validator.NoConditionFileValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileListerTest {

  @DisplayName("listFiles should return empty list when no files are found")
  @Test
  void NotFileFoundTest() throws IOException {
    // given
    FileValidator validator = new NoConditionFileValidator();
    FileLister fileLister = new LocalFileLister(validator);

    // when then
    assertThatThrownBy(() -> fileLister.listFiles(false, "notExistPath"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("path does not exist:");
  }

  @DisplayName("Should get file list when file exists in the directory in 1 depth")
  @Test
  void getAllFileInDirectoryTest() throws IOException {
    // given
    FileValidator validator = new CompositeFileValidator(
      Collections.singletonList(new FileExtensionFilter(List.of(".ndjson")))
    );
    FileLister fileLister = new LocalFileLister(validator);
    // when
    List<String> fileList = fileLister.listFiles(false, "src/test/resources/sample-data");
    // then
    assertThat(fileList).hasSize(3)
      .map(path->Path.of(path).getFileName().toString())
      .containsExactlyInAnyOrder(
        "empty.ndjson",
        "empty-included.ndjson",
        "large.ndjson"
      );
  }

  @DisplayName("Should get all files recursively")
  @Test
  void getFilesRecursiveTest() throws IOException {
    // given
    FileValidator validator = new CompositeFileValidator(
      Collections.singletonList(new FileExtensionFilter(List.of(".ndjson")))
    );
    FileLister fileLister = new LocalFileLister(validator);
    // when
    List<String> fileList = fileLister.listFiles(true, "src/test/resources/sample-data");
    // then
    assertThat(fileList).hasSize(4)
      .map(path->Path.of(path).getFileName().toString())
      .containsExactlyInAnyOrder(
        "empty.ndjson",
        "empty-included.ndjson",
        "large.ndjson",
        "sub1.ndjson"
      );
  }

  @DisplayName("Should get all files when directory and file path are mixed")
  @Test
  void getFilesWhenMixedDirFile() throws IOException {
    // given
    FileValidator validator = new CompositeFileValidator(
      Collections.singletonList(new FileExtensionFilter(List.of(".ndjson")))
    );
    FileLister fileLister = new LocalFileLister(validator);
    // when
    List<String> fileList = fileLister.listFiles(true,
      "src/test/resources/sample-data/subdir1",
      "src/test/resources/sample-data/subdir2/sub22.ndjson"
    );
    // then
    assertThat(fileList).hasSize(2)
      .map(path->Path.of(path).getFileName().toString())
      .containsExactlyInAnyOrder(
        "sub1.ndjson",
        "sub22.ndjson"
      );
  }
}
