package sourceconnector.domain.connect

interface TaskAssignor {
  /**
   * Each task should be assigned by this assignor
   * @param tasks handling the file list
   */
  fun assign(tasks: Collection<Task<FileProcessingResult>>)
}
