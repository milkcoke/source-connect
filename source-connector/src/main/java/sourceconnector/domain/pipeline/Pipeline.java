package sourceconnector.domain.pipeline;

/**
 * Pipeline consists of multiple processors and can get result
 * @param <T> Handling type
 */
public interface Pipeline<T> {
  T getResult();

  /**
   * Move the pipeline to the given offset position
   * @param offset
   * @throws IllegalArgumentException when the given offset is negative
   */
  void toPosition(long offset);
  boolean isComplete();
}
