package sourceconnector.config

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import sourceconnector.config.util.YamlTestUtils.getStringObjectMap
import sourceconnector.repository.file.validator.CompositeFileValidator
import sourceconnector.repository.file.validator.FileValidator
import sourceconnector.repository.file.validator.NoConditionFileValidator
import java.io.IOException

internal class FileSearchConfigsTest {
  @DisplayName("Should throw BindException when missing recursive option")
  @Test
  @Throws(IOException::class)
  fun recursiveOptionMissingTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive:
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    // when then
    assertThatThrownBy {
      binder.bind<FileSearchConfigs?>(
        "source.storage.configs",
        FileSearchConfigs::class.java
      ).get()
    }
      .isInstanceOf(BindException::class.java)
  }

  @DisplayName("Should get recursive option correctly")
  @Test
  @Throws(IOException::class)
  fun recursiveParseTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive: true
            filters:
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))

    // when
    val configs = binder.bind<FileSearchConfigs>("source.storage.configs", FileSearchConfigs::class.java).get()
    // then
    assertThat(configs.isRecursive).isTrue()
  }

  @DisplayName("Should get NoConditionFileValidator when filter are not provided")
  @Test
  @Throws(IOException::class)
  fun noFileValidatorTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive: true
            filters:
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    val configs = binder.bind<FileSearchConfigs>("source.storage.configs", FileSearchConfigs::class.java).get()

    // when
    val fileValidator = configs.toValidator()
    // then
    assertThat<FileValidator>(fileValidator).isInstanceOf(NoConditionFileValidator::class.java)
  }

  @DisplayName("Should get CompositeFileValidator when filter are provided")
  @Test
  @Throws(IOException::class)
  fun compositeFileValidatorCreationTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive: false
            filters:
              - type: extension
                expressions:
                  - '.csv'
                  - '.ndjson'
              - type: exclude
                expressions:
                  - ".*tmp.*"
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    val configs = binder.bind<FileSearchConfigs>("source.storage.configs", FileSearchConfigs::class.java).get()

    // when
    val fileValidator = configs.toValidator()
    // then
    assertThat<FileValidator>(fileValidator).isInstanceOf(CompositeFileValidator::class.java)
  }


  @DisplayName("Get recursive and filters both")
  @Test
  @Throws(IOException::class)
  fun fileSearchConfigMappingTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      source:
        storage:
          type: local
          paths: ['test']
          configs:
            recursive: true
            filters:
              - type: exclude
                expressions:
                  - ".*tmp.*"
      
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))

    // when
    val configs = binder.bind<FileSearchConfigs>("source.storage.configs", FileSearchConfigs::class.java).get()
    // then
    assertThat(configs.isRecursive).isTrue()
    assertThat<FileValidator>(configs.toValidator()).isInstanceOf(CompositeFileValidator::class.java)
  }
}
