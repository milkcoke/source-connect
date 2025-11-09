package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import sourceconnector.config.StorageConfig.StorageType;
import sourceconnector.config.util.YamlTestUtils;

import java.io.IOException;
import java.util.Collections;
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

}
