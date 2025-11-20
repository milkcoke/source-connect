package offsetmanager.service;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.exception.OffsetNotFoundException;
import lombok.RequiredArgsConstructor;
import offsetmanager.domain.offset.OffsetRecord;
import offsetmanager.manager.OffsetManager;
import org.springframework.stereotype.Service;
import offsetmanager.service.dto.LastOffsetRecordBatchResponse;
import offsetmanager.service.dto.LastOffsetRecordResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OffsetManagerService {
  private final OffsetManager offsetManager;

  public LastOffsetRecordResponse readLastOffset(String key) {
    FileKey fileKey = FileKeyParser.parse(key);

    Optional<OffsetRecord> lastOffsetRecord = offsetManager.findLatestOffsetRecord(fileKey);
    if (lastOffsetRecord.isEmpty()) {
      throw new OffsetNotFoundException(key);
    }
    return LastOffsetRecordResponse.from(lastOffsetRecord.get());
  }

  public LastOffsetRecordBatchResponse readLastOffsets(List<String> keys) {
    List<FileKey> fileKeys = keys.stream()
      .map(FileKeyParser::parse)
      .toList();

    List<OffsetRecord> offsetRecordList = this.offsetManager.findLatestOffsetRecords(fileKeys);
    return LastOffsetRecordBatchResponse.from(offsetRecordList);
  }
}
