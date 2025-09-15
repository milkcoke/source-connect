package sourceconnector.domain.offset;

public record FileOffsetRecord(
  String key,
  long offset
) implements OffsetRecord {
}
