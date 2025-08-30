package sourceconnector.service.batcher;

import java.util.Collection;

public interface Batcherable<T> {
  Collection<T> getBatch();
}
