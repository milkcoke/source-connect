package sourceconnector.domain.log;

public class JSONLog extends FileBaseLog {
  private final String payload;
  public JSONLog(String payload, FileMetadata metadata) {
    super(metadata);
    this.payload = payload;
  }

  @Override
  public String get() {
    return this.payload;
  }

}
