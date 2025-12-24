package sourceconnector.service.offset;

import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sourceconnector.domain.connect.OffsetRecordService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OffsetRecordServiceImplTest {

  @DisplayName("Should return empty map when given file key list is empty")
  @Test
  void emptyOffsetMapTest(){
    // given
    OffsetRecordRepository mockedRepository = Mockito.mock(OffsetRecordRepository.class);
    Mockito.when(mockedRepository.findLastOffsetRecords(Mockito.anyList()))
      .thenReturn(Collections.emptyList());

    OffsetRecordService offsetRecordService = new OffsetRecordServiceImpl(mockedRepository);
    // when
    Map<FileKey, Long> offsetMap = offsetRecordService.offsetMap(Collections.emptyList());
    // then
    assertThat(offsetMap).isEmpty();
  }
  @DisplayName("Should return offset map for given file keys")
  @Test
  void offsetMap() {
    // given
    OffsetRecordRepository mockedRepository = Mockito.mock(OffsetRecordRepository.class);
    FileKey fileKey1 = FileKeyParser.parse("file:///file1.txt");
    FileKey fileKey2 = FileKeyParser.parse("file:///file2.txt");

    Mockito.when(mockedRepository.findLastOffsetRecords(Mockito.anyList()))
      .thenReturn(List.of(
        new DefaultOffsetRecord(fileKey1, 100L),
        new DefaultOffsetRecord(fileKey2, 200L)
      ));

    OffsetRecordService offsetRecordService = new OffsetRecordServiceImpl(mockedRepository);

    // when
    Map<FileKey, Long> offsetMap = offsetRecordService.offsetMap(List.of(fileKey1, fileKey2));

    // then
    assertThat(offsetMap).hasSize(2)
      .containsEntry(fileKey1, 100L)
      .containsEntry(fileKey2, 200L);
  }
}
