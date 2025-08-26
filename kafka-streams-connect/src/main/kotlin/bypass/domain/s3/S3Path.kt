package bypass.domain.s3

import com.fasterxml.jackson.annotation.JsonProperty

data class S3Path(
  @JsonProperty("bucket")
  val bucket: String,
  @JsonProperty("path")
  val objectPath: String
)
