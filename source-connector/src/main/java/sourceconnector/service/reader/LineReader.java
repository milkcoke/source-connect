package sourceconnector.service.reader;

public interface LineReader<T> extends Reader<T> {
  int getLineNumber();
  void setLineNumber(int lineNumber);
}
