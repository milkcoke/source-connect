package sourceconnector.domain;

public record S3OffsetRecord(
  String key,
  long offset
) implements OffsetRecord{
}
