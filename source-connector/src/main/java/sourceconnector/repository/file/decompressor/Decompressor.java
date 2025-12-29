package sourceconnector.repository.file.decompressor;

import offsetmanager.domain.file.FileKey;

import java.io.IOException;
import java.io.InputStream;

public interface Decompressor {
  boolean supports(FileKey fileKey);
  InputStream decompress(InputStream in) throws IOException;
}
