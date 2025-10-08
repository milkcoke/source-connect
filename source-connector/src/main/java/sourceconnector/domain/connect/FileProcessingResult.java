package sourceconnector.domain.connect;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FileProcessingResult {
  private int totalCount = 0;
  private int successCount = 0;
  private int failureCount = 0;
}
