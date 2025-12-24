package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "offset-manager")
public record OffsetManagerConfig(
  String baseUrl
) {
}
