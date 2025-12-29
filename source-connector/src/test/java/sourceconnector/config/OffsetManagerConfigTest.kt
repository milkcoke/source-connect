package sourceconnector.config

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import sourceconnector.config.util.YamlTestUtils.getStringObjectMap
import java.io.IOException

internal class OffsetManagerConfigTest {
  // TODO: Add type is missing test
  //   baseUrl omission when type is http test
  @DisplayName("baseUrl omission is not allowed")
  @Test
  @Throws(IOException::class)
  fun yamlConfigurationTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      offsetManager:
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))

    // when
    val offsetManagerConfig = binder.bind<OffsetManagerConfig>("offset-manager", OffsetManagerConfig::class.java).get()
    // then
    Assertions.assertThat(offsetManagerConfig.baseUrl).isNullOrEmpty()
  }

  @DisplayName("offsetManagerBaseUrl should be parsed as URL")
  @Test
  @Throws(IOException::class)
  fun urlParseTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      offsetManager:
        baseUrl: http://localhost:8080
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    // when
    val offsetManagerConfig = binder.bind<OffsetManagerConfig>("offset-manager", OffsetManagerConfig::class.java).get()

    // then
    Assertions.assertThat(offsetManagerConfig.baseUrl).isEqualTo("http://localhost:8080")
  }
}
