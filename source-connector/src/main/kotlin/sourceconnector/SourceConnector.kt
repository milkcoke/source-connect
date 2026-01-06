package sourceconnector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SourceConnector

fun main(args: Array<String>) {
  runApplication<SourceConnector>(*args)
}
