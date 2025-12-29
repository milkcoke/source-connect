package sourceconnector.config

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.factory.FileKeyParser
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "source.storage")
data class StorageConfig(
  val type: StorageType,
  val paths: List<String>
) {
  enum class StorageType {
    LOCAL,
    S3
  }

  val allFileKeys: List<FileKey>
    get() = paths.stream()
      .map<FileKey> { str: String -> FileKeyParser.parse(str) }
      .toList()

  init {
    require(paths.isNotEmpty()) { "paths must not be null or empty" }
  }
}
