package sourceconnector.domain.log;

public class JSONLog implements Log {
  private final String payload;
  private final LogMetadata logMetadata;

  public JSONLog(String payload, LogMetadata logMetadata) {
    if (logMetadata == null) {
      throw new IllegalArgumentException("logMetadata cannot be null");
    }
    this.payload = payload;
    this.logMetadata = logMetadata;
  }

  public JSONLog withPayload(String newPayload) {
    return new JSONLog(newPayload, this.logMetadata);
  }

  @Override
  public LogMetadata getMetadata() {
    return this.logMetadata;
  }

  @Override
  public String get() {
    return this.payload;
  }
}
