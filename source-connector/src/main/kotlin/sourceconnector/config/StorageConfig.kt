package sourceconnector.config

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.factory.FileKeyParser
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "source.storage")
data class StorageConfig(
  val type: StorageType?,
  val paths: List<String>?
) {
  enum class StorageType {
    LOCAL,
    S3
  }

  init {
    require(type != null) { "storage type is required" }
    require(!paths.isNullOrEmpty()) { "paths must not be null or empty" }
  }

  val allFileKeys: List<FileKey>
    get() = paths!!.map { fileKey -> FileKeyParser.parse(fileKey) }
}
