package sourceconnector.domain;

public record LocalFileOffsetRecord(
  String key,
  long offset
) implements OffsetRecord{
}
