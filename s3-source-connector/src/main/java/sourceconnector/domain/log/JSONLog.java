package sourceconnector.domain.log;

public class JSONLog extends FileBaseLog {
  private final String payload;
  public JSONLog(String payload, FileMetadata metadata) {
    super(metadata);
    this.payload = payload;
  }

  public JSONLog withPayload(String newPayload) {
    return new JSONLog(newPayload, this.getMetadata());
  }

  @Override
  public String get() {
    return this.payload;
  }

}
