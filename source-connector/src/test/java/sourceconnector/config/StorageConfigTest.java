package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = StorageConfig.class)
class StorageConfigTest {

  @Autowired
  private StorageConfig storageConfig;

  @DisplayName("Display")
  @Test
  void printTest() {
    // given
    System.out.printf("storageConfig=%s%n", storageConfig);
    // when

    // then
  }
}
