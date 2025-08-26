package bypass

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ByPassApp

fun main(args: Array<String>) {
	runApplication<ByPassApp>(*args)
}
