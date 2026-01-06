package sourceconnector.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "offset-manager")
data class OffsetManagerConfig(
  val type: RepositoryType,
  val baseUrl: String?
) {
  enum class RepositoryType {
    INTERNAL,
    HTTP
  }
}
