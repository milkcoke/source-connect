package offsetmanager.domain.file

class S3Uri(
  private val bucket: String,
  private val key: String
) {

  fun toFileKey(): S3FileKey {
    return S3FileKey(this)
  }

  fun bucket(): String {
    return this.bucket
  }

  fun key(): String {
    return this.key
  }

  override fun toString(): String {
    return "s3://" + this.bucket + "/" + this.key
  }

  companion object {
    @JvmStatic
    fun from(s3Uri: String): S3Uri {
      require(s3Uri.isNotEmpty()) { "S3 URI cannot be null or empty" }
      require(s3Uri.startsWith("s3://")) { "Invalid S3 URI format: $s3Uri" }

      val withoutPrefix = s3Uri.substring(5) // Remove "s3://"
      val slashIndex = withoutPrefix.indexOf('/')
      require(slashIndex != -1) { "Invalid S3 URI format: missing key" }

      val bucket = withoutPrefix.substring(0, slashIndex)
      val key = withoutPrefix.substring(slashIndex + 1)

      return S3Uri(bucket, key)
    }

    @JvmStatic
    fun of(bucket: String, key: String): S3Uri {
      require(bucket.isNotEmpty()) { "Bucket name cannot be null or empty" }
      require(key.isNotEmpty()) { "Key cannot be null or empty" }

      return S3Uri(bucket, key)
    }
  }
}
