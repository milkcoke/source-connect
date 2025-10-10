package sourceconnector.domain.log;

import org.apache.commons.lang3.StringUtils;

public enum EmptyLogMetadata implements LogMetadata {
  INSTANCE;

  @Override
  public String key() {
    return StringUtils.EMPTY;
  }

  @Override
  public long offset() {
    return -1;
  }
}
