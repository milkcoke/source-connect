package sourceconnector.service.reader;

import java.io.IOException;
import java.util.List;

public interface BatchReader<T> extends AutoCloseable {
  /**
   * Read next batch of records
   * @return {@link java.util.List} Batch records
   * @throws IOException
   */
  List<T> nextBatch() throws IOException;

  @Override
  void close() throws IOException;
}
