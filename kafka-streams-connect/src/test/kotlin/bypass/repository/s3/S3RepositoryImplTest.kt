package bypass.repository.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.NoSuchKey
import bypass.domain.s3.S3Path
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class S3RepositoryImplTest {
  private val s3Repository: S3Repository = S3RepositoryImpl(S3Client{
    region = "ap-northeast-2"
  })

  @Test
  fun findByBucketPath() = runTest {
    val s3Path = S3Path(
      bucket = "milkcoke-logs",
      objectPath = "sources/original04.ndjson"
    )
    val byteArray : ByteArray = s3Repository.findByBucketPath(s3Path)
    byteArray.toString(Charsets.UTF_8)
      .trim()
      .split("\n")
      .map { it.toLong() }
      .forEach { println(it) }
  }

  @Test
  fun failedToFindByBucketPath() = runTest {
    val invalidS3Path = S3Path(
      bucket = "milkcoke-logs",
      objectPath = "invalid-path"
    )
     assertThatThrownBy {
       runBlocking {
         s3Repository.findByBucketPath(invalidS3Path)
       }
     }.isInstanceOf(NoSuchKey::class.java)
  }
}
