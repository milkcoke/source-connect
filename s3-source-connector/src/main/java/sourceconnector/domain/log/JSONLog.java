package sourceconnector.domain.log;

public class JSONLog extends FileBaseLog {
  private final String payload;
  public JSONLog(String payload, FileLogMetadata metadata) {
    super(metadata);
    if (payload == null) {
      throw new IllegalArgumentException("Payload cannot be null");
    }
    this.payload = payload;
  }

  @Override
  public JSONLog withPayload(String newPayload) {
    return new JSONLog(newPayload, this.getMetadata());
  }

  @Override
  public String get() {
    return this.payload;
  }

}
