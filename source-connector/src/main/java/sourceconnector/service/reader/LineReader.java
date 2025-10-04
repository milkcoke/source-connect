package sourceconnector.service.reader;

public interface LineReader<T> extends Reader<T> {
  long getLineNumber();
  void setLineNumber(long lineNumber);
}
