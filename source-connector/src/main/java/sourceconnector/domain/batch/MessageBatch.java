package sourceconnector.domain.batch;

import java.util.List;

public interface MessageBatch<T> {
  List<T> get();
}
