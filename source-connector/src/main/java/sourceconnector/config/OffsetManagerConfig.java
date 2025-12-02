package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URL;

@ConfigurationProperties(prefix = "offset-manager")
public record OffsetManagerConfig(
  URL baseUrl
) {
}
