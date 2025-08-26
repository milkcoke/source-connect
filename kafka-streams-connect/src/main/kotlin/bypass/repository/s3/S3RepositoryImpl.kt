package bypass.repository.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toByteArray
import bypass.domain.s3.S3Path
import org.springframework.stereotype.Repository

// TODO: what's suspend keyword
@Repository
class S3RepositoryImpl(
  private val s3Client: S3Client = S3Client {
    region = "ap-northeast-2"
  }
) : S3Repository {
  override suspend fun findByBucketPath(s3Path: S3Path): ByteArray {
    val request = GetObjectRequest {
      bucket = s3Path.bucket
      key = s3Path.objectPath
    }
    return s3Client.getObject(request) {response ->
      response.body!!.toByteArray()
    }
  }
}
