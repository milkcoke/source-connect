package sourceconnector.repository.file.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NoConditionFileValidatorTest {

  @DisplayName("Should always get true in NoConditionFileValidator")
  @Test
  void validateTest() {
    // given
    FileValidator validator = new NoConditionFileValidator();

    // when then
    assertTrue(validator.isValid("sample.ndjson"));
    assertTrue(validator.isValid("test.ndjosn"));
    assertTrue(validator.isValid("sample.csv"));
  }
}
