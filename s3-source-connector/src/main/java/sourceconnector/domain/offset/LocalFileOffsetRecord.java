package sourceconnector.domain.offset;

public record LocalFileOffsetRecord(
  String key,
  long offset
) implements OffsetRecord{
}
