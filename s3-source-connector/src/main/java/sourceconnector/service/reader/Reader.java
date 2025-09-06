package sourceconnector.service.reader;

import java.io.IOException;

public interface Reader<T> extends AutoCloseable {
  /**
   * Read a content of type T
   * @return A content of type T or {@code null} if end of stream has been reached
   * @throws IOException - If an I/O error occurs
   */
  T read() throws IOException;
}
