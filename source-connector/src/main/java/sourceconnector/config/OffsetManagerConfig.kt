package sourceconnector.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "offset-manager")
data class OffsetManagerConfig(
  val repositoryType: RepositoryType,
  val baseUrl: String?
) {
  enum class RepositoryType {
    INTERNAL,
    HTTP
  }
}
