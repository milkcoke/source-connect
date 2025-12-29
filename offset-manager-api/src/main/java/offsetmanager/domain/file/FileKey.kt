package offsetmanager.domain.file

/**
 * Used as File identifier. <br></br>
 * This is also used in OffsetRecord key to identify the file being processed.
 */
interface FileKey : Comparable<FileKey> {
  fun get(): String

  /**
   * Compare based on the String value of the FileKey
   */
  override fun compareTo(other: FileKey): Int {
    return this.get().compareTo(other.get())
  }
}
