package sourceconnector.config

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class ConnectConfigTest {
  @DisplayName("worker count should be positive")
  @Test
  fun workerCountPositiveTest() {
    assertThatThrownBy { ConnectConfig(0, 1) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("workerCount must be >= 1")
  }

  @DisplayName("task count should be greater or equal to the worker count")
  @Test
  fun taskCountTest() {
    assertThatThrownBy { ConnectConfig(2, 1) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("taskCount must be >= workerCount")
  }
}
