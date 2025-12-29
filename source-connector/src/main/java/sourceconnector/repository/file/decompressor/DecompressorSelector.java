package sourceconnector.repository.file.decompressor;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.file.FileKey;

@RequiredArgsConstructor
public enum DecompressorSelector {
  GZIP(new GzipDecompressor()),
  ZIP(new ZipDecompressor()),
  ZSTD(new ZstdDecompressor()),
  NONE(new NoneDecompressor());

  private final Decompressor decompressor;

  public static Decompressor select(FileKey fileKey) {
    for (DecompressorSelector selector : values()) {
      if (selector.decompressor.supports(fileKey)) {
        return selector.decompressor;
      }
    }
    throw new IllegalArgumentException("No supported decompressor found for file: " + fileKey.get());
  }

}
