package sourceconnector.service.reader;

import sourceconnector.exception.FileLogReadException;

import java.io.*;

public class StringLineReader implements LineReader<String> {
  private final LineNumberReader reader;

  public StringLineReader(InputStream inputStream) {
    reader = new LineNumberReader(new InputStreamReader(inputStream));
  }

  /**
   * Seek the line number updating line number <br>
   * Should be called before calling read()
   * @param initialLineNumber position to seek
   * @throws IllegalArgumentException if initial line number be netgative
   * @throws FileLogReadException Failed to readLine() for the input stream.
   */
  public static StringLineReader withInitialLineNumber(
    InputStream inputStream,
    int initialLineNumber
  ) {
    if (initialLineNumber < 0) {
      throw new IllegalArgumentException("initial line number must be greater than or equal to 0");
    }
    StringLineReader stringLineReader = new StringLineReader(inputStream);
    stringLineReader.seekToLine(initialLineNumber);
    return stringLineReader;
  }

  @Override
  public String read() throws IOException {
    return reader.readLine();
  }

  @Override
  public int getLineNumber() {
    return this.reader.getLineNumber();
  }

  @Override
  public void close() throws Exception {
    this.reader.close();
  }

  private void seekToLine(int lineNumber) {
    try {
      for (int i = 0; i < lineNumber; i++) {
        if (this.reader.readLine() == null) {
          throw new IllegalArgumentException("Initial line number exceeds last line number");
        }
      }
    } catch (IOException e) {
      throw new FileLogReadException(e.getMessage(), e);
    }
  }
}
