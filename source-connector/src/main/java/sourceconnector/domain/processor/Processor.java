package sourceconnector.domain.processor;

public interface Processor<I, R> {
  R process(I record);
}
