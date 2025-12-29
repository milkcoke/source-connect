package offsetmanager.repository

import offsetmanager.domain.OffsetStateReadiness
import offsetmanager.domain.OffsetStorage
import offsetmanager.domain.file.FileKey
import offsetmanager.domain.offset.OffsetRecord
import offsetmanager.manager.OffsetManager
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Should update continuously when new offsets are produced to the offset topic <br></br>
 * without consumer group management in the background
 */
@Repository
class OffsetManagerRepository(
  private val offsetStorage: OffsetStorage,
  private val offsetStateReadiness: OffsetStateReadiness
) : OffsetManager {

  override fun findLatestOffsetRecord(key: FileKey): Optional<OffsetRecord> {
    this.offsetStateReadiness.awaitReady()
    return this.offsetStorage.find(key)
  }

  override fun findLatestOffsetRecords(keys: List<FileKey>): List<OffsetRecord> {
    this.offsetStateReadiness.awaitReady()
    return keys.stream()
      .map<Optional<OffsetRecord>> { key: FileKey? -> offsetStorage.find(key!!) }
      .flatMap<OffsetRecord> { obj: Optional<OffsetRecord> -> obj.stream() }
      .toList()
  }
}
