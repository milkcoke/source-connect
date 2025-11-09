package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("source.storage.s3")
public record S3Config(
  String bucket,
  String region
) {
}
