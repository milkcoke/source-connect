package offsetmanager.manager;

import java.util.Optional;

public interface OffsetManager {
  Optional<Long> findLatestOffset(String key);
  void update(String key, long offset);
  void removeKey(String key);
}
