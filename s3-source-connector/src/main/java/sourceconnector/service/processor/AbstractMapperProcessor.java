package sourceconnector.service.processor;

public abstract class AbstractMapperProcessor<T, R> implements BaseProcessor<T, R> {

  abstract protected R map(T record);

  @Override
  public R process(T record) {
    return map(record);
  }
}
