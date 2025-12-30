package sourceconnector.config

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import sourceconnector.config.util.YamlTestUtils.getStringObjectMap
import java.io.IOException

internal class OffsetManagerConfigTest {
  @DisplayName("Should provide protocol")
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

//     when then
    Assertions.assertThatThrownBy {
      binder.bind<OffsetManagerConfig>("offset-manager", OffsetManagerConfig::class.java).get()
    }.isInstanceOf(BindException::class.java)
  }

  @DisplayName("Unsupported type should raise exception")
  @Test
  fun unsupportedTypeTest() {
    val map: Map<String, Any> = getStringObjectMap(
      """
      offsetManager:
        type: unsupported_type
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))

    // when then
    Assertions.assertThatThrownBy {
      binder.bind<OffsetManagerConfig>("offset-manager", OffsetManagerConfig::class.java).get()
    }.isInstanceOf(BindException::class.java)
      .hasRootCauseInstanceOf(IllegalArgumentException::class.java)
  }


  @DisplayName("baseUrl can be null when protocol is INTERNAL")
  @Test
  @Throws(IOException::class)
  fun baseUrlMissingTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      offsetManager:
        type: internal
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    // when
    val offsetManagerConfig = binder.bind<OffsetManagerConfig>("offset-manager", OffsetManagerConfig::class.java).get()

    // then
    assertThat(offsetManagerConfig.type).isSameAs(OffsetManagerConfig.RepositoryType.INTERNAL)
    assertThat(offsetManagerConfig.baseUrl).isNull()
  }

  @DisplayName("type property should be parsed as case insensitive")
  @Test
  @Throws(IOException::class)
  fun ignoreCaseOnTypeProperty() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      offsetManager:
        type: Internal
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    // when
    val offsetManagerConfig = binder.bind<OffsetManagerConfig>("offset-manager", OffsetManagerConfig::class.java).get()

    // then
    assertThat(offsetManagerConfig.type).isSameAs(OffsetManagerConfig.RepositoryType.INTERNAL)
    assertThat(offsetManagerConfig.baseUrl).isNull()
  }

  @DisplayName("type property should be case insensitive")
  @Test
  @Throws(IOException::class)
  fun ignoreCaseOnTypeProperty2() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      offsetManager:
        type: INTERNAL
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    // when
    val offsetManagerConfig = binder.bind<OffsetManagerConfig>("offset-manager", OffsetManagerConfig::class.java).get()

    // then
    assertThat(offsetManagerConfig.type).isSameAs(OffsetManagerConfig.RepositoryType.INTERNAL)
    assertThat(offsetManagerConfig.baseUrl).isNull()
  }

  @DisplayName("offsetManagerBaseUrl should be parsed as URL")
  @Test
  @Throws(IOException::class)
  fun offsetManagerHttpTest() {
    // given
    val map: Map<String, Any> = getStringObjectMap(
      """
      offsetManager:
        type: HTTP
        baseUrl: http://localhost:8080
      """.trimIndent()
    )
    val binder = Binder(MapConfigurationPropertySource(map))
    // when
    val offsetManagerConfig = binder.bind<OffsetManagerConfig>("offset-manager", OffsetManagerConfig::class.java).get()

    // then
    assertThat(offsetManagerConfig.type).isSameAs(OffsetManagerConfig.RepositoryType.HTTP)
    assertThat(offsetManagerConfig.baseUrl).isEqualTo("http://localhost:8080")
  }
}
