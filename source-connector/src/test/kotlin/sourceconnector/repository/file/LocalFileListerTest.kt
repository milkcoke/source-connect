package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey.Companion.from
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sourceconnector.repository.file.filter.FileExtensionFilter
import sourceconnector.repository.file.validator.CompositeFileValidator
import sourceconnector.repository.file.validator.FileValidator
import sourceconnector.repository.file.validator.NoConditionFileValidator
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

internal class LocalFileListerTest {
  @DisplayName("listFiles should return empty list when no files are found")
  @Test
  fun notFileFoundTest() {
    // given
    val validator: FileValidator = NoConditionFileValidator()
    val fileLister: FileLister = LocalFileLister(validator)
    val filePath = Paths.get("notExistPath.txt")
    val fileKey: FileKey = from(filePath)
    // when then
    Assertions.assertThatThrownBy { fileLister.listFiles(fileKey) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining("path does not exist:")
  }

  @DisplayName("Should get file list when file exists in the directory in 1 depth")
  @Test
  @Throws(IOException::class)
  fun getAllFileInDirectoryTest() {
    // given
    val validator: FileValidator = CompositeFileValidator(listOf(FileExtensionFilter(listOf(".ndjson")))
    )
    val fileLister: FileLister = LocalFileLister(validator)
    val localPath = Path.of("src/test/resources/sample-data")
    val fileKey: FileKey = from(localPath)
    // when
    val fileKeys: List<FileKey> = fileLister.listFiles(fileKey)

    // then
    assertThat<FileKey?>(fileKeys).hasSize(3)
      .containsExactlyInAnyOrder(
        from(Path.of("src/test/resources/sample-data/empty.ndjson")),
        from(Path.of("src/test/resources/sample-data/empty-included.ndjson")),
        from(Path.of("src/test/resources/sample-data/large.ndjson"))
      )
  }

  @DisplayName("Should get all files recursively")
  @Test
  @Throws(IOException::class)
  fun getFilesRecursiveTest() {
    // given
    val validator: FileValidator = CompositeFileValidator(listOf(FileExtensionFilter(listOf(".ndjson"))))
    val fileLister: FileLister = LocalFileLister(validator)
    val localPath = Path.of("src/test/resources/sample-data")
    val fileKey: FileKey = from(localPath)
    // when
    val fileKeys: List<FileKey> = fileLister.listFilesRecursively(fileKey)
    // then
    assertThat<FileKey>(fileKeys).hasSize(6)
      .containsExactlyInAnyOrder(
        from(Path.of("src/test/resources/sample-data/empty.ndjson")),
        from(Path.of("src/test/resources/sample-data/empty-included.ndjson")),
        from(Path.of("src/test/resources/sample-data/large.ndjson")),
        from(Path.of("src/test/resources/sample-data/subdir1/sub1.ndjson")),
        from(Path.of("src/test/resources/sample-data/subdir2/sub2.ndjson")),
        from(Path.of("src/test/resources/sample-data/subdir2/sub22.ndjson"))
      )
  }

  @DisplayName("Should get all files when directory and file path are mixed")
  @Test
  @Throws(IOException::class)
  fun getFilesWhenMixedDirFile() {
    // given
    val validator: FileValidator = CompositeFileValidator(
      listOf(FileExtensionFilter(listOf(".ndjson")))
    )
    val fileLister: FileLister = LocalFileLister(validator)
    // when
    val fileList: List<FileKey> = fileLister.listFilesRecursively(
      from(Path.of("src/test/resources/sample-data/subdir1")),
      from(Path.of("src/test/resources/sample-data/subdir2/sub22.ndjson"))
    )
    // then
    assertThat<FileKey>(fileList).hasSize(2)
      .containsExactlyInAnyOrder(
        from(Path.of("src/test/resources/sample-data/subdir1/sub1.ndjson")),
        from(Path.of("src/test/resources/sample-data/subdir2/sub22.ndjson"))
      )
  }
}
