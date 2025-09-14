package sourceconnector.domain.offset;

public interface OffsetRecord {
  String key();
  long offset();
}
