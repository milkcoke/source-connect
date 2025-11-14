package sourceconnector.repository.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.file.FileKey;
import sourceconnector.domain.file.LocalFileKey;
import sourceconnector.repository.file.filter.FileExtensionFilter;
import sourceconnector.repository.file.validator.CompositeFileValidator;
import sourceconnector.repository.file.validator.FileValidator;
import sourceconnector.repository.file.validator.NoConditionFileValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileListerTest {

  @DisplayName("listFiles should return empty list when no files are found")
  @Test
  void NotFileFoundTest() {
    // given
    FileValidator validator = new NoConditionFileValidator();
    FileLister fileLister = new LocalFileLister(validator);
    Path filePath = Paths.get("notExistPath.txt");
    FileKey fileKey = LocalFileKey.from(filePath);
    // when then
    assertThatThrownBy(() -> fileLister.listFiles(fileKey))
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
    Path localPath = Path.of("src/test/resources/sample-data");
    FileKey fileKey = LocalFileKey.from(localPath);
    // when
    List<FileKey> fileKeys = fileLister.listFiles(fileKey);

    // then
    assertThat(fileKeys).hasSize(3)
      .containsExactlyInAnyOrder(
        LocalFileKey.from(Path.of("src/test/resources/sample-data/empty.ndjson")),
        LocalFileKey.from(Path.of("src/test/resources/sample-data/empty-included.ndjson")),
        LocalFileKey.from(Path.of("src/test/resources/sample-data/large.ndjson"))
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
    Path localPath = Path.of("src/test/resources/sample-data");
    FileKey fileKey = LocalFileKey.from(localPath);
    // when
    List<FileKey> fileKeys = fileLister.listFilesRecursively(fileKey);
    // then
    assertThat(fileKeys).hasSize(6)
      //TODO: Apply equals and hasCode according to the get() String result
      .containsExactlyInAnyOrder(
        LocalFileKey.from(Path.of("src/test/resources/sample-data/empty.ndjson")),
        LocalFileKey.from(Path.of("src/test/resources/sample-data/empty-included.ndjson")),
        LocalFileKey.from(Path.of("src/test/resources/sample-data/large.ndjson")),
        LocalFileKey.from(Path.of("src/test/resources/sample-data/sub1.ndjson")),
        LocalFileKey.from(Path.of("src/test/resources/sample-data/sub2.ndjson")),
        LocalFileKey.from(Path.of("src/test/resources/sample-data/sub22.ndjson"))
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
    List<FileKey> fileList = fileLister.listFilesRecursively(
      LocalFileKey.from(Path.of("src/test/resources/sample-data/subdir1")),
      LocalFileKey.from(Path.of("src/test/resources/sample-data/subdir2/sub22.ndjson"))
    );
    // then
    assertThat(fileList).hasSize(2)
      .containsExactlyInAnyOrder(
        LocalFileKey.from(Path.of("src/test/resources/sample-data/subdir1/sub1.ndjson")),
        LocalFileKey.from(Path.of("src/test/resources/sample-data/subdir2/sub22.ndjson"))
      );
  }
}
