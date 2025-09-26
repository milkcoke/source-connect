package service;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.OffsetManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RemoteOffsetService {
  private final OffsetManager offsetManager;

  public Optional<Long> readLastOffset(String key) {
    return offsetManager.findLatestOffset(key);
  }
}
