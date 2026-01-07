package offsetmanager.domain.file.factory

import offsetmanager.domain.file.LocalFileKey
import offsetmanager.domain.file.S3FileKey
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class FileKeyParserTest {
  @DisplayName("Windows C drive style path local file key toUri creation test")
  @Test
  fun windowsLocalFIleKeyPathTest() {
    val fileKey = FileKeyParser.parse("file:///C:/path/to/file.txt")
    assertThat(fileKey).isInstanceOf(LocalFileKey::class.java)
    assertThat(fileKey.get()).isEqualTo("file:///C:/path/to/file.txt")
  }

  @DisplayName("LocalFileKey should be created when file URI as local file path")
  @Test
  fun localFileKeyPathTest() {
    val fileKey = FileKeyParser.parse("file:///path/to/file.txt")
    assertThat(fileKey).isInstanceOf(LocalFileKey::class.java)
    assertThat(fileKey.get()).isEqualTo("file:///path/to/file.txt")
  }

  @DisplayName("Should throw IllegalArgumentException when schema is missing")
  @Test
  fun schemaMissingTest() {
    // given
    assertThatThrownBy({ FileKeyParser.parse("/path/to/file.txt") })
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Unsupported file key schema: " + "/path/to/file.txt")
  }

  @DisplayName("S3FileKey should be created when S3 Uri provided")
  @Test
  fun s3FileKeyPathTest() {
    val fileKey = FileKeyParser.parse("s3://my-bucket/path/to/file.txt")
    assertThat(fileKey).isInstanceOf(S3FileKey::class.java)
  }
}
