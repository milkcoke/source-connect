package sourceconnector.domain.log;

public interface Metadata {

  static Metadata empty() {
    return Empty.INSTANCE;
  }

  enum Empty implements Metadata {
    INSTANCE
  }
}
