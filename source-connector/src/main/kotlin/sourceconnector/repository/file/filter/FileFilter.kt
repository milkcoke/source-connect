package sourceconnector.repository.file.filter

import offsetmanager.domain.file.FileKey

fun interface FileFilter {
  fun accept(fileKey: FileKey): Boolean
}
