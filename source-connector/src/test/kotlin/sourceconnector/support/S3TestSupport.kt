package sourceconnector.support

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.BucketLocationConstraint
import aws.sdk.kotlin.services.s3.model.CreateBucketRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import aws.smithy.kotlin.runtime.net.url.Url
import kotlinx.coroutines.runBlocking

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import sourceconnector.repository.file.S3Location
import java.nio.file.Path


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class S3TestSupport {
  protected val BUCKET_NAME: String = "test-bucket"
  protected val REGION: String = "ap-northeast-2"
  protected lateinit var s3Client: S3Client

  @BeforeAll
  fun initS3Client() {
    // Initialize the AWS SDK for Kotlin client
    s3Client = S3Client {
      // The region is a string
      region = REGION
      // The endpoint must be a parsed URL
      endpointUrl = Url.parse(localStackContainer.endpoint.toString())      // The credentials provider has a Kotlin-idiomatic way of setting it
      credentialsProvider = StaticCredentialsProvider {
        accessKeyId = "test-access-key"
        secretAccessKey = "test-secret-access-key"
      }
    }

    // The createBucket call is a suspend function.
    // Since @BeforeAll methods cannot be suspend functions, we must use runBlocking
    // to bridge the synchronous test world with the asynchronous SDK world.
    runBlocking {
      val request = CreateBucketRequest {
        bucket = BUCKET_NAME
        createBucketConfiguration {
          locationConstraint = BucketLocationConstraint.fromValue(REGION)
        }
      }
      s3Client.createBucket(request)
    }
  }

  @AfterAll
  protected fun cleanS3Client() {
    s3Client.close()
  }

  protected suspend fun upload(s3Location: S3Location, path: Path) {
    // Use the Kotlin DSL for building requests.
    val request = PutObjectRequest {
      bucket = s3Location.bucket
      key = s3Location.key
      // The body is provided via a ByteStream
      body = ByteStream.fromFile(path.toFile())
    }
    s3Client.putObject(request)
  }

  companion object {
    /** static fields will be shared between test methods.
     * Started only once before any test methods are executed and stopped after the last test method has executed.
     */
    @Container
    @JvmStatic
    protected val localStackContainer: LocalStackContainer =
      LocalStackContainer(
        DockerImageName.parse("localstack/localstack:s3-latest") // Define which AWS Service is enabled.
      ).withServices("s3")
  }
}
