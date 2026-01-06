package sourceconnector.support

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import sourceconnector.repository.file.S3Location
import java.net.URI
import java.nio.file.Path


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class S3TestSupport {
  protected val BUCKET_NAME: String = "test-bucket"
  protected val REGION: Region = Region.AP_NORTHEAST_2
  protected lateinit var s3Client: S3Client

  @BeforeAll
  fun initS3Client() {
    val s3Endpoint: URI = localStackContainer.endpoint

    s3Client = S3Client.builder() // Redirect endpoint to that localstack container provides
      .endpointOverride(s3Endpoint)
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create("test-access-key", "test-secret-key")
        )
      )
      .region(REGION)
      .build()


    // TODO: why not throw BucketAlreadyExistsException?
    s3Client.createBucket(
      CreateBucketRequest.builder()
        .bucket(BUCKET_NAME)
        .build()
    )
  }

  @AfterAll
  protected fun cleanS3Client() {
    s3Client.close()
  }

  fun upload(s3Location: S3Location, path: Path?) {
    val response: PutObjectResponse? = s3Client.putObject(
      { builder: PutObjectRequest.Builder ->
        builder
          .bucket(s3Location.bucket)
          .key(s3Location.key)
          .build()
      },
      RequestBody.fromFile(path)
    )
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
