package sourceconnector.domain.connect;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;
import offsetmanager.domain.offset.OffsetStatus;
import sourceconnector.repository.offset.OffsetRecordRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class FileTaskAssignor implements TaskAssignor {
  private final List<FileKey> sortedAllFilePaths;
  private final int totalTaskCount;
  private final OffsetRecordRepository offsetRecordRepository;

  /**
   * All file path should be ordered since assign files to the task without duplication.
   * @param fileKeys handled by the tasks
   * @param totalTaskCount all task count
   */
  public FileTaskAssignor(
    List<FileKey> fileKeys,
    int totalTaskCount,
    OffsetRecordRepository offsetRecordRepository
  ) {
    this.sortedAllFilePaths = fileKeys.stream()
      .sorted()
      .toList();
    this.totalTaskCount = totalTaskCount;
    this.offsetRecordRepository = offsetRecordRepository;
  }

  @Override
  public final void assign(Collection<Task<FileProcessingResult>> tasks) {
    // TODO: Apply service instead of Repository lambda call here
    try {
      List<OffsetRecord> offsetRecords = this.offsetRecordRepository.findLastOffsetRecords(sortedAllFilePaths);
      Map<FileKey, Long> offsetMap = offsetRecords.stream()
        .collect(Collectors.toMap(OffsetRecord::key, OffsetRecord::offset));


      for (var task : tasks) {
        int taskIndex = task.getIndex();

        int quotient = this.sortedAllFilePaths.size() / totalTaskCount;
        int remainder = this.sortedAllFilePaths.size() % totalTaskCount;

        int startIndex = taskIndex * quotient + Math.min(taskIndex, remainder);
        int endIndex = (taskIndex + 1) * quotient + Math.min(taskIndex + 1, remainder);

        List<FileKey> fileKeys = this.sortedAllFilePaths.subList(startIndex, endIndex);
        // TODO : can exclude offset value when it is COMPLETED value here
        Map<FileKey, Long> fileKeyOffsetMap = fileKeys.stream()
          .collect(Collectors.toMap(
            fileKey -> fileKey,
            fileKey -> offsetMap.getOrDefault(fileKey, OffsetStatus.INITIAL.getValue())
          ));

        task.assign(fileKeyOffsetMap);
      }
      log.info("Completed all task assignments");

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
