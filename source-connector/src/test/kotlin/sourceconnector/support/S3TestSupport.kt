package sourceconnector.support

import com.adobe.testing.s3mock.testcontainers.S3MockContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import sourceconnector.repository.file.S3Location
import java.net.URI
import java.nio.file.Path


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class S3TestSupport {
  protected lateinit var s3Client: S3Client

  @BeforeAll
  fun initS3Client() {
    val s3Endpoint: URI = URI.create(s3MockContainer.httpEndpoint)

    s3Client = S3Client.builder() // Redirect endpoint to that S3Mock container provides
      .endpointOverride(s3Endpoint)
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create("test-access-key", "test-secret-key")
        )
      )
      // The container is reachable by mapped port, not by virtual host.
      .serviceConfiguration(
        S3Configuration.builder()
          .pathStyleAccessEnabled(true)
          .build()
      )
      .region(REGION)
      .build()
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
    @JvmStatic
    protected val BUCKET_NAME: String = "test-bucket"

    @JvmStatic
    protected val REGION: Region = Region.AP_NORTHEAST_2

    /** static fields will be shared between test methods.
     * Started only once before any test methods are executed and stopped after the last test method has executed.
     */
    @Container
    @JvmStatic
    protected val s3MockContainer: S3MockContainer =
      S3MockContainer("5.1.0")
        .withInitialBuckets(BUCKET_NAME) // Replaces the explicit createBucket call.
        .withRegion(REGION.id())
  }
}
