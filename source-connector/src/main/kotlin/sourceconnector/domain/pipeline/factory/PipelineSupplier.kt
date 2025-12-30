package sourceconnector.domain.pipeline.factory

import offsetmanager.domain.file.FileKey
import sourceconnector.domain.pipeline.Pipeline

/**
 * Provides simple factory API to create [Pipeline] instance. <br></br>
 * Pipeline should be created per file handled by the [sourceconnector.domain.connect.Task]
 */
fun interface PipelineSupplier<T> {
  fun get(fileKey: FileKey): Pipeline<T?>
}
