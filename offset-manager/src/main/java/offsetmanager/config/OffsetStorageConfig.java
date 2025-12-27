package offsetmanager.config;

import offsetmanager.domain.InMemoryOffsetStorage;
import offsetmanager.domain.OffsetStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OffsetStorageConfig {
  @Bean
  public OffsetStorage inmemoryOffsetStorage() {
    return new InMemoryOffsetStorage();
  }
}
