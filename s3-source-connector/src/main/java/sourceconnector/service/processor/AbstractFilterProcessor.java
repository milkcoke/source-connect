package sourceconnector.service.processor;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractFilterProcessor<T> implements BaseProcessor<T, Optional<T>> {
  Predicate<T> condition;

  @Override
  public Optional<T> process(T record) {
    return condition.test(record) ? Optional.of(record) : Optional.empty();
  }
}
