package offsetmanager.config

import offsetmanager.domain.OffsetStateUpdater
import offsetmanager.domain.OffsetStateUpdaterImpl
import offsetmanager.domain.OffsetStorage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class OffsetStateUpdaterConfig {
  @Bean
  fun offsetStateUpdater(
    offsetTopicName: String,
    consumerProperties: Properties,
    offsetStorage: OffsetStorage
  ): OffsetStateUpdater {
    return OffsetStateUpdaterImpl(
      offsetTopicName,
      consumerProperties,
      offsetStorage
    )
  }
}
