package sourceconnector.domain.processor;

public abstract class AbstractFilterProcessor<Log> extends BaseProcessor<Log> {
  protected abstract boolean condition(Log input);

  @Override
  public Log process(Log input) {
    if(!this.condition(input)) return null;
    if(this.next == null) return input;
    return this.next.process(input);
  }
}
