package offsetmanager.service;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.exception.OffsetNotFoundException;
import offsetmanager.domain.offset.DefaultOffsetRecord;
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
    FileKey fileKey = FileKeyParser.parse("file:///existKey.txt");

    when(mockManager.findLatestOffsetRecord(fileKey))
      .thenReturn(Optional.of(new DefaultOffsetRecord(fileKey, 10L)));

    OffsetManagerService remoteOffsetService = new OffsetManagerService(mockManager);
    // when
    LastOffsetRecordResponse lastOffsetRecordResponse = remoteOffsetService.readLastOffset("file:///existKey.txt");
    // then
    assertThat(lastOffsetRecordResponse.key()).isEqualTo("file:///existKey.txt");
    assertThat(lastOffsetRecordResponse.offset()).isEqualTo(10L);
  }

  @DisplayName("Should throw OffsetNotFoundException when the key does not exist")
  @Test
  void returnEmptyWhenNotExistKey() {
    // given
    OffsetManager mockManager = Mockito.mock(OffsetManager.class);
    FileKey nonExistFileKey = FileKeyParser.parse("file:///nonExistKey.txt");
    when(mockManager.findLatestOffsetRecord(nonExistFileKey)).thenReturn(Optional.empty());

    OffsetManagerService remoteOffsetService = new OffsetManagerService(mockManager);
    // when then
    assertThatThrownBy(()-> remoteOffsetService.readLastOffset("file:///notExistKey.txt"))
      .isInstanceOf(OffsetNotFoundException.class)
      .hasMessage("Offset not found for key: file:///notExistKey.txt");

  }
}
