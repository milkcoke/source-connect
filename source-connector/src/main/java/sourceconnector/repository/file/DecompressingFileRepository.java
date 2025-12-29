package sourceconnector.repository.file;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.file.FileKey;
import sourceconnector.repository.file.decompressor.Decompressor;
import sourceconnector.repository.file.decompressor.DecompressorSelector;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class DecompressingFileRepository implements FileRepository {
  private final FileRepository delegate;

  @Override
  public InputStream getFile(FileKey fileKey) throws IOException {
    InputStream inputStream = delegate.getFile(fileKey);
    Decompressor decompressor = DecompressorSelector.select(fileKey);
    return decompressor.decompress(inputStream);
  }
}
