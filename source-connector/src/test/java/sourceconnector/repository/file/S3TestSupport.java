package sourceconnector.repository.file;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.net.URI;
import java.nio.file.Path;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
abstract class S3TestSupport {
  protected final String BUCKET_NAME = "test-bucket";
  protected final Region REGION = Region.AP_NORTHEAST_2;
  protected S3Client s3Client;

  /** static fields will be shared between test methods.
   * Started only once before any test methods are executed and stopped after the last test method has executed.
    */
  @Container
  protected static final LocalStackContainer localStackContainer = new LocalStackContainer(
    DockerImageName.parse("localstack/localstack:s3-latest")
    // Define which AWS Service is enabled.
  ).withServices(S3);

  @BeforeAll
  protected void initS3Client() {
    URI s3Endpoint = localStackContainer.getEndpointOverride(S3);

    s3Client = S3Client.builder()
      // Redirect endpoint to that localstack container provides
      .endpointOverride(s3Endpoint)
      .credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create("test-access-key", "test-secret-key")
      ))
      .region(REGION)
      .build();

    // TODO: why not throw BucketAlreadyExistsException?
    s3Client.createBucket(CreateBucketRequest.builder()
      .bucket(BUCKET_NAME)
      .build());
  }

  @AfterAll
  protected void cleanS3Client() {
    if (s3Client != null) {
      s3Client.close();
    }
  }

  public void upload(String key, String content) {
    s3Client.putObject(
      builder -> builder.bucket(BUCKET_NAME).key(key).build(),
      RequestBody.fromString(content)
    );
  }

  public void upload(String key, Path path) {
    s3Client.putObject(
      builder -> builder.bucket(BUCKET_NAME).key(key).build(),
      RequestBody.fromFile(path)
    );
  }

}
