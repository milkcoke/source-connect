package sourceconnector.service.reader;

import java.io.*;

public class StringLineReader implements LineReader<String> {
  private final LineNumberReader reader;

  public StringLineReader(InputStream inputStream) {
    reader = new LineNumberReader(new InputStreamReader(inputStream));
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
  public void setLineNumber(int lineNumber) {
    this.reader.setLineNumber(lineNumber);
  }

  @Override
  public void close() throws Exception {
    this.reader.close();
  }
}
