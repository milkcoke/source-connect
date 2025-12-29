package sourceconnector.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConnectConfigTest {

  @DisplayName("worker count should be positive")
  @Test
  void workerCountPositiveTest() {
    assertThatThrownBy(()-> new ConnectConfig(0, 1))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("workerCount must be >= 1");
  }

  @DisplayName("task count should be greater or equal to the worker count")
  @Test
  void taskCountTest() {
    assertThatThrownBy(()-> new ConnectConfig(2, 1))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("taskCount must be >= workerCount");
  }
}
