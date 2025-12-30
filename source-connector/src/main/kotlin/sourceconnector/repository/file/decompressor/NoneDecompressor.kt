package sourceconnector.repository.file.decompressor

import offsetmanager.domain.file.FileKey
import java.io.InputStream

class NoneDecompressor : Decompressor {
  override fun supports(fileKey: FileKey): Boolean {
    return true
  }

  override fun decompress(`in`: InputStream): InputStream {
    return `in`
  }
}
