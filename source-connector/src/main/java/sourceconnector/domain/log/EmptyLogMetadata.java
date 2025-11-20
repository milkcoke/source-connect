package sourceconnector.domain.log;

import offsetmanager.domain.file.FileKey;

public enum EmptyLogMetadata implements LogMetadata {
  INSTANCE;

  @Override
  public FileKey key() {
    // FIXME: return a proper empty FileKey if needed
    return null;
  }

  @Override
  public long offset() {
    return -1;
  }
}
