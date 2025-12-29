package offsetmanager.domain.file

class S3FileKey(
  private val s3Uri: S3Uri
) : FileKey {

  override fun get(): String {
    return this.s3Uri.toString()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is S3FileKey) return false

    return this.get() == other.get()
  }

  override fun hashCode(): Int {
    return this.get().hashCode()
  }
}
