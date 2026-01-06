package sourceconnector.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("source.storage.s3")
data class S3Config(
  val region: String
)
