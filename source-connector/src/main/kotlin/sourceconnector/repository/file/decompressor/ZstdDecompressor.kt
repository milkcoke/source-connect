package sourceconnector.repository.file.decompressor

import com.github.luben.zstd.ZstdInputStream
import offsetmanager.domain.file.FileKey
import java.io.IOException
import java.io.InputStream

class ZstdDecompressor : Decompressor {
  override fun supports(fileKey: FileKey): Boolean {
    return fileKey.get().endsWith(".zst")
  }

  @Throws(IOException::class)
  override fun decompress(`in`: InputStream): InputStream {
    return ZstdInputStream(`in`)
  }
}
