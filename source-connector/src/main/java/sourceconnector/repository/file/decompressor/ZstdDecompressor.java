package sourceconnector.repository.file.decompressor;

import com.github.luben.zstd.ZstdInputStream;
import offsetmanager.domain.file.FileKey;

import java.io.IOException;
import java.io.InputStream;

public class ZstdDecompressor implements Decompressor {
  @Override
  public boolean supports(FileKey fileKey) {
    return fileKey.get().endsWith(".zst");
  }

  @Override
  public InputStream decompress(InputStream in) throws IOException {
    return new ZstdInputStream(in);
  }
}
