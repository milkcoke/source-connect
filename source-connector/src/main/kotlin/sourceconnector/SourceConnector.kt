package sourceconnector

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@SpringBootApplication
@ConfigurationPropertiesScan
class SourceConnector
fun main(args: Array<String>) {
  SpringApplication.run(SourceConnector::class.java, *args)
}
