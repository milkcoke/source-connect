package sourceconnector.repository.file.decompressor

import offsetmanager.domain.file.FileKey
import java.io.IOException
import java.io.InputStream

interface Decompressor {
  fun supports(fileKey: FileKey): Boolean

  @Throws(IOException::class)
  fun decompress(`in`: InputStream): InputStream
}
