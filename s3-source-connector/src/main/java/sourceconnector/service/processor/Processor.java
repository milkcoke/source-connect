package sourceconnector.service.processor;

public interface Processor<I, O, F> {
  F process(I record);
}
