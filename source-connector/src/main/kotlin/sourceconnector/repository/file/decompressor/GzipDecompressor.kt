package sourceconnector.repository.file.decompressor

import offsetmanager.domain.file.FileKey
import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream

class GzipDecompressor : Decompressor {
  override fun supports(fileKey: FileKey): Boolean {
    return fileKey.get().endsWith(".gz")
  }

  @Throws(IOException::class)
  override fun decompress(`in`: InputStream): InputStream {
    return GZIPInputStream(`in`)
  }
}
