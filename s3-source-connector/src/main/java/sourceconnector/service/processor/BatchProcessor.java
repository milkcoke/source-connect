package sourceconnector.service.processor;

import java.util.List;

public interface BatchProcessor<T, R> {
  /**
   * Process batch of records
   * @param {@link java.util.List} Batch to process
   */
  List<R> processBatch(List<T> batch);
}
