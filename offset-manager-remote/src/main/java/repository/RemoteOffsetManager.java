package repository;

import lombok.RequiredArgsConstructor;
import offsetmanager.domain.OffsetManager;
import org.apache.kafka.clients.consumer.Consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class RemoteOffsetManager implements OffsetManager {
  private final Map<String, Long> offsetStore = new HashMap<>();
  private final Consumer<String, Long> consumer;

  @Override
  public Optional<Long> findLatestOffset(String key) {
    if (this.offsetStore.containsKey(key)) {
      return Optional.of(this.offsetStore.get(key));
    }

    return Optional.empty();
  }

  @Override
  public void update(String key, long offset) {
    this.offsetStore.put(key, offset);
  }

  @Override
  public void removeKey(String key) {
    this.offsetStore.remove(key);
  }
}
