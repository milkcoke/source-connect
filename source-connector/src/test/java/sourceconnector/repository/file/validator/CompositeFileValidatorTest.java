package sourceconnector.repository.file.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.repository.file.filter.FileExcludeFilter;
import sourceconnector.repository.file.filter.FileExtensionFilter;
import sourceconnector.repository.file.filter.FileIncludeFilter;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class CompositeFileValidatorTest {

  @DisplayName("Should get only file all conditions are passed")
  @Test
  void allConditionTest() {
    FileValidator validator = new CompositeFileValidator(List.of(
      new FileExtensionFilter(List.of(".ndjson", ".csv")),
      new FileIncludeFilter(List.of(".*sample.*"))
    ));

    assertAll(
      () -> assertThat(validator.isValid("sample.ndjson")).isTrue(),
      () -> assertThat(validator.isValid("test.ndjson")).isFalse(),
      () -> assertThat(validator.isValid("sample.csv")).isTrue()
    );
  }

  @DisplayName("Should provide one or more condition")
  @Test
  void provideNoFilterTest() {
    // given
    assertThatThrownBy(()-> new CompositeFileValidator(Collections.emptyList()))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("File filter condition cannot be null or empty");
  }
}
