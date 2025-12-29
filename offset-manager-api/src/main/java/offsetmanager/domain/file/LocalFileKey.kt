package offsetmanager.domain.file

import java.net.URI
import java.nio.file.Path

 class LocalFileKey(
  private val uri: URI
) : FileKey {

  override fun get(): String {
    return uri.toString()
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o !is LocalFileKey) return false

    return this.get() == o.get()
  }

  override fun hashCode(): Int {
    return this.get().hashCode()
  }

  companion object {
    @JvmStatic
    fun from(path: Path): LocalFileKey {
      return LocalFileKey(path.toUri())
    }
  }
}
