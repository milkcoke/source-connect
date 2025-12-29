package sourceconnector.repository.file.decompressor;

import offsetmanager.domain.file.FileKey;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GzipDecompressor implements Decompressor {
  @Override
  public boolean supports(FileKey fileKey) {
    return fileKey.get().endsWith(".gz");
  }

  @Override
  public InputStream decompress(InputStream in) throws IOException {
    return new GZIPInputStream(in);
  }
}
