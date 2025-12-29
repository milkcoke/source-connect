package sourceconnector.repository.file.decompressor;

import offsetmanager.domain.file.FileKey;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipDecompressor implements Decompressor {
  @Override
  public boolean supports(FileKey fileKey) {
    return fileKey.get().endsWith(".zip");
  }

  @Override
  public InputStream decompress(InputStream in) throws IOException {
    ZipInputStream zipInputStream = new ZipInputStream(in);
    ZipEntry zipEntry = zipInputStream.getNextEntry();
    if (zipEntry == null) {
      throw new IOException("Empty zip file");
    }
    return zipInputStream;
  }
}
