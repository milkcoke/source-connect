package sourceconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("source.storage.blob")
public record BlogConfig(
  String container
) {
}
