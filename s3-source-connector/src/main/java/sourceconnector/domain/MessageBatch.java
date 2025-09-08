package sourceconnector.domain;

import java.util.List;

public interface MessageBatch<T> {
  List<T> get();
}
