package sourceconnector.service.reader;

import java.io.*;

public class StringLineReader implements LineReader<String> {
  // FIXME: LineNumberReader only support Integer range about line number
  private final LineNumberReader reader;

  public StringLineReader(InputStream inputStream) {
    reader = new LineNumberReader(new InputStreamReader(inputStream));
  }

  @Override
  public String read() throws IOException {
    return reader.readLine();
  }

  @Override
  public long getLineNumber() {
    return this.reader.getLineNumber();
  }

  @Override
  public void setLineNumber(long lineNumber) {
    this.reader.setLineNumber((int) lineNumber);
  }

  @Override
  public void close() throws Exception {
    this.reader.close();
  }
}
