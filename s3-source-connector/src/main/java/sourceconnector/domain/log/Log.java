package sourceconnector.domain.log;

import org.apache.commons.lang3.StringUtils;

/**
 * Log Should have the payload and metadata
 */
public interface Log {
  String get();
  Metadata getMetadata();

  static Log empty() {
    return Empty.INSTANCE;
  }
  enum Empty implements Log {
    INSTANCE;
    @Override public String get() { return StringUtils.EMPTY; }
    @Override public Metadata getMetadata() { return Metadata.empty(); }
  }

}
