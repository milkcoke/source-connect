package sourceconnector.service.processor;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class ChainBatchProcessor implements BatchProcessor<String, String>{
  private final List<Processor<String, String, String>> processors;

  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    private final List<Processor<String, String, String>> processors = new ArrayList<>();

    public Builder addProcessor(Processor<String,String, String> processor) {
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
    for (Processor<String, String> processor : processors) {
      result = batch.stream()
        .map(processor::process)
        .filter(Objects::nonNull)
        .toList();
    }

    return result;
  }

}
