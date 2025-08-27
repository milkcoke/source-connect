package sourceconnector.service.processor;

public interface BaseProcessor<T, R> {
  R process(T  record);
}
