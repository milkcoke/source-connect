package sourceconnector.service.batcher;

import java.util.Collection;

public interface Batchable<T> {
  Collection<T> nextBatch();
}
