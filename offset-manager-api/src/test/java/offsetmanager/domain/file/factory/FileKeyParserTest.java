package offsetmanager.domain.file.factory;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;
import offsetmanager.domain.file.S3FileKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class FileKeyParserTest {

  @DisplayName("LocalFileKey should be created when file URI as local file path")
  @Test
  void localFileKeyPathTest() {
    FileKey fileKey = FileKeyParser.parse("file:///path/to/file.txt");
    assertThat(fileKey).isInstanceOf(LocalFileKey.class);
  }

  @DisplayName("S3FileKey should be created when S3 Uri provided")
  @Test
  void S3FileKeyPathTest() {
    FileKey fileKey = FileKeyParser.parse("s3://my-bucket/path/to/file.txt");
    assertThat(fileKey).isInstanceOf(S3FileKey.class);
  }

  @DisplayName("Should throw IllegalArgumentException when unsupported URI scheme provided")
  @Test
  void invalidFileKeyPathTest() {
    assertThatThrownBy(()-> FileKeyParser.parse("invalid file key string"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Unsupported file key schema: " + "invalid file key string");
  }
}
