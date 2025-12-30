package sourceconnector.repository.file

import offsetmanager.domain.file.FileKey
import sourceconnector.repository.file.decompressor.DecompressorSelector.Companion.select
import java.io.IOException
import java.io.InputStream

class DecompressingFileRepository(
  private val delegate: FileRepository
) : FileRepository {

  @Throws(IOException::class)
  override fun getFile(fileKey: FileKey): InputStream {
    val inputStream = delegate.getFile(fileKey)
    val decompressor = select(fileKey)
    return decompressor.decompress(inputStream)
  }
}
