package offsetmanager.domain;

public record DefaultOffsetRecord(
  String key,
  long offset
) implements OffsetRecord {
}
