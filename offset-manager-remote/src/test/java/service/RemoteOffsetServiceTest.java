package service;

import offsetmanager.domain.DefaultOffsetRecord;
import offsetmanager.domain.OffsetRecord;
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
    when(mockManager.findLatestOffsetRecord("existKey"))
      .thenReturn(Optional.of(new DefaultOffsetRecord("existKey", 10L)));

    RemoteOffsetService remoteOffsetService = new RemoteOffsetService(mockManager);
    // when
    Optional<OffsetRecord> lastOffset = remoteOffsetService.readLastOffset("existKey");
    // then
    assertThat(lastOffset.get()).isEqualTo(new DefaultOffsetRecord("existKey", 10L));
  }

  @DisplayName("Should return empty when the key does not exist")
  @Test
  void returnEmptyWhenNotExistKey() {
    // given
    OffsetManager mockManager = Mockito.mock(OffsetManager.class);
    when(mockManager.findLatestOffsetRecord("notExistKey")).thenReturn(Optional.empty());

    RemoteOffsetService remoteOffsetService = new RemoteOffsetService(mockManager);
    // when
    Optional<OffsetRecord> lastOffsetRecord = remoteOffsetService.readLastOffset("notExistKey");

    // then
    assertThat(lastOffsetRecord).isEmpty();
  }
}
