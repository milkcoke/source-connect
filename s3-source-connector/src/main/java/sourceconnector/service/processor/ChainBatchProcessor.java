package sourceconnector.service.processor;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class ChainBatchProcessor implements BatchProcessor<String, String>{
  private final List<BaseProcessor<String, String>> processors;

  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    private final List<BaseProcessor<String, String>> processors = new ArrayList<>();

    public Builder addProcessor(BaseProcessor<String,String> processor) {
      processors.add(processor);
      return this;
    }
    public ChainBatchProcessor build() {
      if (processors.isEmpty()) throw new IllegalStateException("Should pass at least one processor");
      return new ChainBatchProcessor(processors);
    }

  }



  @Override
  public List<String> processBatch(List<String> batch) {
    List<String> result = batch;
    for (BaseProcessor<String, String> processor : processors) {
      result = batch.stream()
        .map(processor::process)
        .filter(Objects::nonNull)
        .toList();
    }

    return result;
  }

}
