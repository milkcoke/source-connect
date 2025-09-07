package sourceconnector.domain;

public record FileOffsetRecord(
  String key,
  long offset
) implements OffsetRecord {
}
