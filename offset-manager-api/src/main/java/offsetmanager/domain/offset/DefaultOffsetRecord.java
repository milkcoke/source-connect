package offsetmanager.domain.offset;

public record DefaultOffsetRecord(
  String key,
  long offset
) implements OffsetRecord {
}
