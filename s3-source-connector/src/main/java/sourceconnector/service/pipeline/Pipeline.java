package sourceconnector.service.pipeline;

/**
 * Pipeline consists of multiple processors and can get result
 * @param <T> Handling type
 */
public interface Pipeline<T> {
  T getResult();
}
