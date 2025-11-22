package sourceconnector.repository.offset.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;

import java.util.List;
import java.util.Optional;

public interface OffsetRecordRepository {
    Optional<OffsetRecord> findLastOffsetRecord(FileKey key);
    List<OffsetRecord> findLastOffsetRecords(List<FileKey> keys) throws JsonProcessingException;
}
