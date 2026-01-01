package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.S3Uri
import offsetmanager.domain.file.S3Uri.Companion.from

data class S3Location(
  val bucket: String,
  val key: String
) {
  fun toFileKey(): FileKey {
    return S3Uri.of(bucket, key).toFileKey()
  }

  init {
    require(bucket.isNotBlank()) { "Bucket must not be blank" }
    require(key.isNotBlank()) { "S3 Key must not be blank" }
  }

  companion object {
    @JvmStatic
    fun from(s3Uri: S3Uri): S3Location {
      return S3Location(s3Uri.bucket(), s3Uri.key())
    }

    @JvmStatic
    fun from(fileKey: FileKey): S3Location {
      val s3Uri = from(fileKey.get())
      return S3Location(s3Uri.bucket(), s3Uri.key())
    }
  }
}
