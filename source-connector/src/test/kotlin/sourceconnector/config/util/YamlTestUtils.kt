package sourceconnector.config.util

import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.boot.origin.OriginTrackedValue
import org.springframework.core.io.ByteArrayResource
import java.io.IOException
import kotlin.collections.mapValues

object YamlTestUtils {

  @Throws(IOException::class)
  fun getStringObjectMap(yamlStr: String): Map<String, Any> {
    val propertySource = YamlPropertySourceLoader()
      .load("test", ByteArrayResource(yamlStr.toByteArray()))
      .first()

    @Suppress("UNCHECKED_CAST")
    return unwrapOriginTrackedValues(propertySource.source as Map<String, Any>)
  }

  private fun unwrapOriginTrackedValues(
    source: Map<String, Any>
  ): Map<String, Any> =
    source.mapValues { (_, v) ->
      (v as? OriginTrackedValue)?.value ?: v
    }
}
