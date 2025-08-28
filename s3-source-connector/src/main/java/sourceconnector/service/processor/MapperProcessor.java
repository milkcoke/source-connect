package sourceconnector.service.processor;

import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class MapperProcessor<I, O, F> extends BaseProcessor<I, O, F> {
  private final Function<I, O> mapper;

  @Override
  public F process(I record) {
    O output = this.mapper.apply(record);
    if (this.next != null) {
      return this.next.process(output);
    }
    return (F) output;
  }
}
