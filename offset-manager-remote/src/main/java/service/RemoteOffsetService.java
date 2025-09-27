package service;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.OffsetRecord;
import offsetmanager.manager.OffsetManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RemoteOffsetService {
  private final OffsetManager offsetManager;

  public Optional<OffsetRecord> readLastOffset(String key) {
    return offsetManager.findLatestOffsetRecord(key);
  }
}
