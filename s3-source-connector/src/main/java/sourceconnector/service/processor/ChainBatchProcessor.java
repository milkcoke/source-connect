package sourceconnector.service.processor;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
      List<BaseProcessor<String, String>> reversed = new ArrayList<>(processors);
      Collections.reverse(reversed);
      return new ChainBatchProcessor(reversed);
    }

  }



  @Override
  public List<String> processBatch(List<String> batch) {
  }

}
