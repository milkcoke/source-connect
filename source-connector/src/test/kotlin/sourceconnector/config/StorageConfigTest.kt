package sourceconnector.config

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.LocalFileKey
import offsetmanager.domain.file.S3FileKey
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import sourceconnector.config.util.YamlTestUtils.getStringObjectMap
import java.io.IOException

internal class StorageConfigTest {
  @DisplayName("Should get storage mapping according to yaml string")
  @Test
  @Throws(IOException::class)
  fun storageConfigMappingTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
    source:
      storage:
        type: s3
        paths:
          - s3://my-bucket/foo
          - s3://my-bucket/bar
    """.trimIndent()
    )

    val binder = Binder(MapConfigurationPropertySource(map))
    // when
    val config = binder.bind<StorageConfig>("source.storage", StorageConfig::class.java).get()

    // then
    assertThat<StorageConfig.StorageType>(config.type).isEqualTo(StorageConfig.StorageType.S3)
    assertThat<String>(config.paths).containsExactlyInAnyOrder(
      "s3://my-bucket/foo",
      "s3://my-bucket/bar"
    )
  }


  @DisplayName("Should throw NPE when type is missing in the yaml")
  @Test
  @Throws(IOException::class)
  fun storageTypeConfigMissingTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
    source:
      storage:
        paths:
          - s3://my-bucket/foo
          - s3://my-bucket/bar
    """.trimIndent()
    )

    val binder = Binder(MapConfigurationPropertySource(map))
    // when then
    assertThatThrownBy {
      binder.bind<StorageConfig>(
        "source.storage",
        StorageConfig::class.java
      ).get()
    }
      .hasRootCauseInstanceOf(IllegalArgumentException::class.java)
      .hasStackTraceContaining("storage type is required")
  }


  @DisplayName("Should get LocalFileKey list according to type config")
  @Test
  fun getLocalFileKeysFromPathsTest() {
    // given
    val storageConfig = StorageConfig(
      StorageConfig.StorageType.LOCAL,
      listOf("file:///Users/downloads", "file:///Users/downloads/sample.csv")
    )
    // when
    val fileKeys: List<FileKey> = storageConfig.allFileKeys

    // then
    assertThat<FileKey>(fileKeys).hasExactlyElementsOfTypes(
      LocalFileKey::class.java,
      LocalFileKey::class.java
    )
  }

  @DisplayName("Should get FileKey List according to type config")
  @Test
  fun getS3FileKeysFromPathsTest() {
    // given
    val storageConfig = StorageConfig(
      StorageConfig.StorageType.S3,
      listOf("s3://my-bucket/foo/", "s3://my-bucket/bar/sample.csv")
    )
    // when
    val fileKeys: List<FileKey> = storageConfig.allFileKeys

    // then
    assertThat<FileKey>(fileKeys).hasExactlyElementsOfTypes(
      S3FileKey::class.java,
      S3FileKey::class.java
    )
  }

  @DisplayName("Should throw IllegalArgumentException when different type path is provided")
  @Test
  fun failS3FileKeyWhenLocalPathsTest() {
    // given
    val storageConfig = StorageConfig(
      StorageConfig.StorageType.S3,
      listOf("s3://my-bucket/foo/", "Users/Downloads/sample.csv")
    )

    // when then
    assertThatThrownBy{storageConfig.allFileKeys}
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("Unsupported file key schema: " + "Users/Downloads/sample.csv")
  }
}
