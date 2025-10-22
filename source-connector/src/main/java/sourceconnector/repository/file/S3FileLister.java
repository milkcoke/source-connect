package sourceconnector.repository.file;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import sourceconnector.config.S3Config;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class S3FileLister implements FileLister  {
  private final S3Client s3Client;
  private final String bucket;

  public S3FileLister(S3Config s3Config) {
    this.s3Client = S3Client.builder()
      .region(Region.of(s3Config.region()))
      .build();
    this.bucket = s3Config.bucket();
  }

  /**
   * Get all s3 object key paths <br>
   * this can handle both directory and file path
   * @return {@code List<String>}
   */
  @Override
  public List<String> listFiles(boolean recursive, String... paths) {

    List<String> objectPaths = new ArrayList<>();

    for (String path : paths) {
      ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
        .bucket(this.bucket)
        .prefix(path);

      if (!recursive) {
        requestBuilder.delimiter("/");
      }

      ListObjectsV2Request request = requestBuilder.build();

      List<String> keys = this.s3Client.listObjectsV2Paginator(request)
        .stream()
        .flatMap(response -> response.contents().stream())
        .map(S3Object::key)
        .toList();

      objectPaths.addAll(keys);
    }

    return objectPaths;
  }
}
