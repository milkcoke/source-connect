package sourceconnector.service.processor;

import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class FilterProcessor<T> extends BaseProcessor<T, T, T> {
  private Predicate<T> predicate;

  @Override
  public T process(T record) {
    if (!predicate.test(record)) return null;
    if (next == null) {return record;}
    this.next.process(record);
  }
}
