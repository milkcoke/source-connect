package sourceconnector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class S3SourceConnector {
  public static void main(String[] args) {
    SpringApplication.run(S3SourceConnector.class, args);
  }
}
