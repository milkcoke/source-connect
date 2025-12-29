package offsetmanager

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class OffsetManagerApplication {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      SpringApplication.run(OffsetManagerApplication::class.java, *args)
    }
  }
}
