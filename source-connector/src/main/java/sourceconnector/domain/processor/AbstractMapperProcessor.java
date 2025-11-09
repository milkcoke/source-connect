package sourceconnector.domain.processor;

public abstract class AbstractMapperProcessor<Log> extends BaseProcessor<Log> {

  protected abstract Log map(Log input);

  @Override
  public Log process(Log input) {
    Log result = this.map(input);
    if (this.next != null) {
      return this.next.process(result);
    }
    return result;
  }
}
