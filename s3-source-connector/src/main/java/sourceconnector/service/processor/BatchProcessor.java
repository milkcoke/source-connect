package sourceconnector.service.processor;

import java.util.List;

public interface BatchProcessor<T> {
  /**
   * Process batch of records
   * @param {@link java.util.List} Batch to process
   */
  void processBatch(List<T> batch);
}
