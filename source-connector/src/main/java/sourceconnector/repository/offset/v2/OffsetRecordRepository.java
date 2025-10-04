package sourceconnector.repository.offset.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import offsetmanager.domain.OffsetRecord;

import java.util.List;
import java.util.Optional;

public interface OffsetRecordRepository {
    Optional<OffsetRecord> findLastOffsetRecord(String key);
    List<OffsetRecord> findLastOffsetRecords(List<String> keys) throws JsonProcessingException;
}
