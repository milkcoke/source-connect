package service;

import offsetmanager.manager.OffsetManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class RemoteOffsetServiceTest {

  @DisplayName("Should return offset when the key exists")
  @Test
  void readLastOffset() {
    // given
    OffsetManager mockManager = Mockito.mock(OffsetManager.class);
    when(mockManager.findLatestOffset("existKey")).thenReturn(Optional.of(10L));

    RemoteOffsetService remoteOffsetService = new RemoteOffsetService(mockManager);
    // when
    Optional<Long> lastOffset = remoteOffsetService.readLastOffset("existKey");
    // then
    assertThat(lastOffset.get()).isEqualTo(10L);
  }

  @DisplayName("Should return empty when the key does not exist")
  @Test
  void returnEmptyWhenNotExistKey() {
    // given
    OffsetManager mockManager = Mockito.mock(OffsetManager.class);
    when(mockManager.findLatestOffset("notExistKey")).thenReturn(Optional.empty());

    RemoteOffsetService remoteOffsetService = new RemoteOffsetService(mockManager);
    // when
    Optional<Long> lastOffset = remoteOffsetService.readLastOffset("notExistKey");

    // then
    assertThat(lastOffset).isEmpty();
  }
}
