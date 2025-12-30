package sourceconnector.service.offset

import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.factory.FileKeyParser.Companion.parse
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import sourceconnector.domain.connect.OffsetRecordService

internal class OffsetRecordServiceImplTest {
  @DisplayName("Should return empty map when given file key list is empty")
  @Test
  fun emptyOffsetMapTest() {
    // given
    val mockedRepository = Mockito.mock<OffsetRecordRepository>(OffsetRecordRepository::class.java)
    Mockito.`when`<List<OffsetRecord>>(mockedRepository.findLastOffsetRecords(Mockito.anyList<FileKey>()))
      .thenReturn(listOf())

    val offsetRecordService: OffsetRecordService = OffsetRecordServiceImpl(mockedRepository)
    // when
    val offsetMap: Map<FileKey, Long> = offsetRecordService.offsetMap(mutableListOf<FileKey>())
    // then
    assertThat(offsetMap).isEmpty()
  }

  @DisplayName("Should return offset map for given file keys")
  @Test
  fun offsetMap() {
    // given
    val mockedRepository = Mockito.mock<OffsetRecordRepository>(OffsetRecordRepository::class.java)
    val fileKey1 = parse("file:///file1.txt")
    val fileKey2 = parse("file:///file2.txt")

    Mockito.`when`<List<OffsetRecord>>(mockedRepository.findLastOffsetRecords(Mockito.anyList<FileKey>()))
      .thenReturn(
        listOf(
          DefaultOffsetRecord(fileKey1, 100L),
          DefaultOffsetRecord(fileKey2, 200L)
        )
      )

    val offsetRecordService: OffsetRecordService = OffsetRecordServiceImpl(mockedRepository)

    // when
    val offsetMap: Map<FileKey, Long> = offsetRecordService.offsetMap(listOf(fileKey1, fileKey2))

    // then
    assertThat<FileKey, Long>(offsetMap).hasSize(2)
      .containsEntry(fileKey1, 100L)
      .containsEntry(fileKey2, 200L)
  }
}
