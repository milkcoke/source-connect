package sourceconnector.domain;

import java.util.Collection;

public interface MessageBatch<T> {
  Collection<T> get();
}
