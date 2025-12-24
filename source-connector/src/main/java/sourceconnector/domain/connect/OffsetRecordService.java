package sourceconnector.domain.connect;

import offsetmanager.domain.file.FileKey;

import java.util.List;
import java.util.Map;

public interface OffsetRecordService {
  Map<FileKey, Long> offsetMap(List<FileKey> fileKeys);
}
