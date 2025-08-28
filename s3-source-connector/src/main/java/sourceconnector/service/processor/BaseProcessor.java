package sourceconnector.service.processor;

public abstract class BaseProcessor<I, O, F> implements Processor<I, O, F> {
  protected BaseProcessor<O, ?, F> next;

  @SuppressWarnings("unchecked")
  protected <NO> BaseProcessor<I, NO, F> setNext(BaseProcessor<O, NO, F> next) {
    this.next = next;
    return (BaseProcessor<I, NO, F>) this;
  }

  public abstract F process(I input);
}
