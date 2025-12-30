package sourceconnector.repository.file.decompressor

import offsetmanager.domain.file.FileKey

enum class DecompressorSelector(private val decompressor: Decompressor) {
  GZIP(GzipDecompressor()),
  ZIP(ZipDecompressor()),
  ZSTD(ZstdDecompressor()),
  NONE(NoneDecompressor()); // Semicolon is needed to separate enum constants from other members

  companion object {
    /**
     * Selects the first decompressor that supports the given file key.
     * @throws IllegalArgumentException if no supported decompressor is found.
     */
    @JvmStatic
    fun select(fileKey: FileKey): Decompressor {
      return entries
        .firstOrNull { selector -> selector.decompressor.supports(fileKey) }
        ?.decompressor
        ?: throw IllegalArgumentException("No supported decompressor found for file: ${fileKey.get()}")
    }
  }
}
