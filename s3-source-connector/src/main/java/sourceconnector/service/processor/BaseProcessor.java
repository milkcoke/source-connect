package sourceconnector.service.processor;

public abstract class BaseProcessor<T> implements Processor<T, T> {
  protected BaseProcessor<T> next;

  public BaseProcessor<T> setNext(BaseProcessor<T> nextProcessor) {
    this.next = nextProcessor;
    return nextProcessor;
  }

  /**
   * Process input and return the final output
   */
  public abstract T process(T input);
}
