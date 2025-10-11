package sourceconnector.domain.connect;

import lombok.Getter;
import lombok.Setter;

@Getter
public class FileProcessingResult {
  @Setter
  private int totalCount = 0;
  private int successCount = 0;
  private int failureCount = 0;

  public void addSuccessCount() {
    successCount++;
  }

  public void addFailureCount() {
    failureCount++;
  }

}
