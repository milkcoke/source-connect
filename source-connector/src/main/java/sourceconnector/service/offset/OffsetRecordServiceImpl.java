package sourceconnector.service.offset;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;
import sourceconnector.domain.connect.OffsetRecordService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OffsetRecordServiceImpl implements OffsetRecordService {
  private final OffsetRecordRepository offsetRecordRepository;

  @Override
  public Map<FileKey, Long> offsetMap(List<FileKey> fileKeys) {
    List<OffsetRecord> offsetRecords = this.offsetRecordRepository.findLastOffsetRecords(fileKeys);
    return offsetRecords.stream()
      .collect(Collectors.toMap(
        OffsetRecord::key,
        OffsetRecord::offset
      ));
  }
}
