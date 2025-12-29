package offsetmanager.config;

import offsetmanager.domain.OffsetStateUpdater;
import offsetmanager.domain.OffsetStateUpdaterImpl;
import offsetmanager.domain.OffsetStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class OffsetStateUpdaterConfig {
  @Bean
  OffsetStateUpdater offsetStateUpdater(
    String offsetTopicName,
    Properties consumerProperties,
    OffsetStorage offsetStorage
  ) {
    return new OffsetStateUpdaterImpl(
      offsetTopicName,
      consumerProperties,
      offsetStorage
    );
  }
}
