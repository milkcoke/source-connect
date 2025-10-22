package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.storage.s3")
public record S3Config(
  String bucket,
  String region
) {
}
