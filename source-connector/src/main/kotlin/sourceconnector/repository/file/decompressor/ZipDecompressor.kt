package sourceconnector.repository.file.decompressor

import offsetmanager.domain.file.FileKey
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream

class ZipDecompressor : Decompressor {
  override fun supports(fileKey: FileKey): Boolean {
    return fileKey.get().endsWith(".zip")
  }

  @Throws(IOException::class)
  override fun decompress(`in`: InputStream): InputStream {
    val zipInputStream = ZipInputStream(`in`)
    zipInputStream.getNextEntry() ?: throw IOException("Empty zip file")
    return zipInputStream
  }
}
