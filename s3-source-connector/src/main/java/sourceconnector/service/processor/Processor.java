package sourceconnector.service.processor;

public interface Processor<I, R> {
  R process(I record);
}
