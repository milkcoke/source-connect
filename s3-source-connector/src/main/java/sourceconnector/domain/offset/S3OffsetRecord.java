package sourceconnector.domain.offset;

public record S3OffsetRecord(
  String key,
  long offset
) implements OffsetRecord{
}
