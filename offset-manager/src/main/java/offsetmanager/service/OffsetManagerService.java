package offsetmanager.service;

import offsetmanager.exception.OffsetNotFoundException;
import lombok.RequiredArgsConstructor;
import offsetmanager.domain.OffsetRecord;
import offsetmanager.manager.OffsetManager;
import org.springframework.stereotype.Service;
import offsetmanager.service.dto.LastOffsetRecordBatchResponse;
import offsetmanager.service.dto.LastOffsetRecordResponse;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OffsetManagerService {
  private final OffsetManager offsetManager;

  public LastOffsetRecordResponse readLastOffset(String key) {
    Optional<OffsetRecord> lastOffsetRecord = offsetManager.findLatestOffsetRecord(key);
    if (lastOffsetRecord.isEmpty()) {
      throw new OffsetNotFoundException(key);
    }
    return LastOffsetRecordResponse.from(lastOffsetRecord.get());
  }

  public LastOffsetRecordBatchResponse readLastOffsets(List<String> keys) {
    List<OffsetRecord> offsetRecordList = this.offsetManager.findLatestOffsetRecords(keys);
    return LastOffsetRecordBatchResponse.from(offsetRecordList);
  }
}
