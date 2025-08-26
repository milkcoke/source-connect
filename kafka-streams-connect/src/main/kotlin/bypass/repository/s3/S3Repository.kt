package bypass.repository.s3

import bypass.domain.s3.S3Path

interface S3Repository {
  suspend fun findByBucketPath(s3Path: S3Path): ByteArray
}
