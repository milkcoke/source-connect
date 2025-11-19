package sourceconnector.config;

import offsetmanager.domain.file.S3FileKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import sourceconnector.config.StorageConfig.StorageType;
import sourceconnector.config.util.YamlTestUtils;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.LocalFileKey;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageConfigTest {

  @DisplayName("Should get storage mapping according to yaml string")
  @Test
  void storageConfigMappingTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
    source:
      storage:
        type: s3
        paths:
          - s3://my-bucket/foo
          - s3://my-bucket/bar
    """);

    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when
    StorageConfig config = binder.bind("source.storage", StorageConfig.class).get();

    // then
    assertThat(config.type()).isEqualTo(StorageType.S3);
    assertThat(config.paths()).containsExactlyInAnyOrder(
      "s3://my-bucket/foo",
      "s3://my-bucket/bar"
    );
  }

  @DisplayName("Failed to construct StorageConfig when type is missing")
  @Test
  void storageTypeMissingTest() {
    assertThatThrownBy(()-> new StorageConfig(null, Collections.emptyList()))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("storage type is required");
  }

  @DisplayName("Should throw NPE when type is missing in the yaml")
  @Test
  void storageTypeConfigMissingTest() throws IOException {
    // given
    Map<String, Object> map = YamlTestUtils.getStringObjectMap("""
    source:
      storage:
        paths:
          - s3://my-bucket/foo
          - s3://my-bucket/bar
    """);

    Binder binder = new Binder(new MapConfigurationPropertySource(map));
    // when then
    assertThatThrownBy(()-> binder.bind("source.storage", StorageConfig.class).get())
      .hasRootCauseInstanceOf(NullPointerException.class)
      .hasStackTraceContaining("storage type is required");
  }


  @DisplayName("Should get LocalFileKey list according to type config")
  @Test
  void getLocalFileKeysFromPathsTest() {
    // given
    StorageConfig storageConfig = new StorageConfig(
      StorageType.LOCAL,
      List.of("Users/downloads", "Users/downloads/sample.csv")
    );
    // when
    List<FileKey> fileKeys = storageConfig.getAllFileKeys();

    // then
    assertThat(fileKeys).hasExactlyElementsOfTypes(
      LocalFileKey.class,
      LocalFileKey.class
    );
  }

  @DisplayName("Should get FileKey List according to type config")
  @Test
  void getS3FileKeysFromPathsTest() {
    // given
    StorageConfig storageConfig = new StorageConfig(
      StorageType.S3,
      List.of("s3://my-bucket/foo/", "s3://my-bucket/bar/sample.csv")
    );
    // when
    List<FileKey> fileKeys = storageConfig.getAllFileKeys();

    // then
    assertThat(fileKeys).hasExactlyElementsOfTypes(
      S3FileKey.class,
      S3FileKey.class
    );
  }

  @DisplayName("Should throw IllegalArgumentException when different type path is provided")
  @Test
  void failS3FileKeyWhenLocalPathsTest() {
    // given
    StorageConfig storageConfig = new StorageConfig(
      StorageType.S3,
      List.of("s3://my-bucket/foo/", "Users/Downloads/sample.csv")
    );

    // when then
    assertThatThrownBy(storageConfig::getAllFileKeys)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Invalid S3 URI format: " + "Users/Downloads/sample.csv");
  }

}
