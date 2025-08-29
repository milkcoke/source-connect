package sourceconnector.service.processor;

public abstract class BaseProcessor<I, O> implements Processor<I, O> {
  protected BaseProcessor<O, ?> next;

  /**
   * Set the next processor in the chain.
   * @param nextProcessor processor whose input matches this processor's output
   * @param <NO> next processor output type
   * @return the next processor (for chaining)
   */
  @SuppressWarnings("unchecked")
  public <NO> BaseProcessor<O, NO> setNext(BaseProcessor<O, NO> nextProcessor) {
    this.next = nextProcessor;
    return nextProcessor;
  }

  /**
   * Process input and return the final output
   */
  public abstract O process(I input);
}
