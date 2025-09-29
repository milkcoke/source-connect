package offsetmanager.service;

import offsetmanager.exception.OffsetNotFoundException;
import offsetmanager.domain.DefaultOffsetRecord;
import offsetmanager.manager.OffsetManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import offsetmanager.service.dto.LastOffsetRecordResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class OffsetManagerRepositoryTest {

  @DisplayName("Should return offset when the key exists")
  @Test
  void readLastOffset() {
    // given
    OffsetManager mockManager = Mockito.mock(OffsetManager.class);
    when(mockManager.findLatestOffsetRecord("existKey"))
      .thenReturn(Optional.of(new DefaultOffsetRecord("existKey", 10L)));

    OffsetManagerService remoteOffsetService = new OffsetManagerService(mockManager);
    // when
    LastOffsetRecordResponse lastOffsetRecordResponse = remoteOffsetService.readLastOffset("existKey");
    // then
    assertThat(lastOffsetRecordResponse.key()).isEqualTo("existKey");
    assertThat(lastOffsetRecordResponse.offset()).isEqualTo(10L);
  }

  @DisplayName("Should throw OffsetNotFoundException when the key does not exist")
  @Test
  void returnEmptyWhenNotExistKey() {
    // given
    OffsetManager mockManager = Mockito.mock(OffsetManager.class);
    when(mockManager.findLatestOffsetRecord("notExistKey")).thenReturn(Optional.empty());

    OffsetManagerService remoteOffsetService = new OffsetManagerService(mockManager);
    // when then
    assertThatThrownBy(()-> remoteOffsetService.readLastOffset("notExistKey"))
      .isInstanceOf(OffsetNotFoundException.class)
      .hasMessage("Offset not found for key: notExistKey");

  }
}
