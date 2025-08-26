package sourceconnector.domain;

public interface OffsetRecord {
  String key();
  long offset();
}
