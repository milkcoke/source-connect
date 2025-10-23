package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.config.util.YamlTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FiltersConfigTest {

  @DisplayName("Should get NoFileValidator when filter are not defined")
  @Test
  void noFileValidatorTest() throws IOException {
    // given
    YamlTestUtils.getStringObjectMap("""
      
      """);
    // when

    // then
  }

}
