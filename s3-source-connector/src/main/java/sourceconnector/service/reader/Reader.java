package sourceconnector.service.reader;

import java.io.IOException;

public interface Reader<T> extends AutoCloseable {
  T read() throws IOException;
}
