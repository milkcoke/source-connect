package sourceconnector.repository.file.decompressor;

import offsetmanager.domain.file.FileKey;

import java.io.InputStream;

public class NoneDecompressor implements Decompressor {
  @Override
  public boolean supports(FileKey fileKey) {
    return true;
  }

  @Override
  public InputStream decompress(InputStream in) {
    return in;
  }
}
