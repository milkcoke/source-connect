package sourceconnector.service.offset;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.OffsetRecord;

import java.util.List;
import java.util.Optional;

public interface OffsetRecordRepository extends AutoCloseable{
  Optional<OffsetRecord> findLastOffsetRecord(FileKey key);
  List<OffsetRecord> findLastOffsetRecords(List<FileKey> keys);
}
