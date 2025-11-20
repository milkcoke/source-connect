package offsetmanager.controller;

import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.exception.OffsetNotFoundException;
import offsetmanager.service.dto.LastOffsetRecordBatchResponse;
import offsetmanager.service.dto.LastOffsetRecordResponse;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OffsetManagerControllerTest extends ControllerTestSupport {

  @DisplayName("Should throw 404 Not Found when the offset record does not exist")
  @Test
  void OffsetNotFoundTest() throws Exception {
    // given
    when(offsetManagerService.readLastOffset("notExistKey"))
      .thenThrow(new OffsetNotFoundException("notExistKey"));

    String responsePayload = mockMvc.perform(
      // when
      MockMvcRequestBuilders.get("/v1/offset-records")
        .param("key", "notExistKey")
    )
      .andExpect(status().isNotFound())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // then
    JSONAssert.assertEquals(
"""
        {
          "statusCode": 404,
          "type": "OFFSET_NOT_FOUND",
          "message": "Offset not found for key: notExistKey"
        }
        """,
      responsePayload,
      JSONCompareMode.STRICT
    );

  }

  @DisplayName("Should return 400 Bad Request when the key parameter is invalid")
  @Test
  void invalidKeyRequestTest() throws Exception {
    // given
    String responsePayload = mockMvc.perform(
      // when
      MockMvcRequestBuilders.get("/v1/offset-records")
       .param("key", "a")
    )
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andReturn()
      .getResponse()
      .getContentAsString();

    JSONAssert.assertEquals(
      """
        {
          "statusCode": 400,
          "type": "INVALID_PARAMETER",
          "message": "Invalid parameters",
          "properties":{
            "key": "Key must be at least 5 length"
          }
        }
        """,
      responsePayload,
      JSONCompareMode.STRICT
    );
  }

  @DisplayName("Should get last offset record successfully")
  @Test
  void lastOffsetReturnTest() throws Exception {
    // given
    // FIXME: URL 형식의 key가 들어올 때 . C:/ (Drive name) 생성되는 부분 수정 필요
    when(offsetManagerService.readLastOffset("file:///path/to/file.txt"))
      .thenReturn(LastOffsetRecordResponse.from(
        new DefaultOffsetRecord(FileKeyParser.parse("file:///path/to/file.txt"), 5L))
      );

    // when
    String responsePayload = mockMvc.perform(
      MockMvcRequestBuilders.get("/v1/offset-records")
        .param("key", "file:///path/to/file.txt")
    ).andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // then
    JSONAssert.assertEquals(
      """
        {
          "key": "file:///path/to/file.txt",
          "offset": 5
        }
        """,
      responsePayload,
      JSONCompareMode.STRICT
    );
  }

  @DisplayName("Should get each last offset record successfully in batch")
  @Test
  void BatchLastOffsetReturnTest() throws Exception {
    // given
    when(offsetManagerService.readLastOffsets(List.of("file:///key1", "file:///key2", "file:///key3")))
      .thenReturn(
        LastOffsetRecordBatchResponse.from(List.of(
          new DefaultOffsetRecord(FileKeyParser.parse("file:///key1"), 5L),
          new DefaultOffsetRecord(FileKeyParser.parse("file:///key2"), 3L),
          new DefaultOffsetRecord(FileKeyParser.parse("file:///key3"), -1L)
        ))
      );

    // when
    String responsePayload = mockMvc.perform(
      MockMvcRequestBuilders.post("/v1/offset-records")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "keys": ["file:///key1", "file:///key2", "file:///key3"]
          }
          """)
    ).andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // then
    JSONAssert.assertEquals(
      """
        {
          "lastOffsetRecords": [
            {
              "key": "file:///key1",
              "offset": 5
            },
            {
              "key": "file:///key2",
              "offset": 3
            },
            {
              "key": "file:///key3",
              "offset": -1
            }
          ]
        }
        """,
      responsePayload,
      JSONCompareMode.STRICT
    );
  }

  @DisplayName("Return status 200 when request is valid even though retrieved list is empty")
  @Test
  void batchResponseEmptyTest() throws Exception {
    // given
    when(offsetManagerService.readLastOffsets(List.of("file:///key1", "file:///key2", "file:///key3")))
      .thenReturn(
        LastOffsetRecordBatchResponse.from(Collections.emptyList())
      );

    // when
    String responsePayload = mockMvc.perform(
      MockMvcRequestBuilders.post("/v1/offset-records")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "keys": ["file:///key1", "file:///key2", "file:///key3"]
          }
          """)
    ).andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // then
    JSONAssert.assertEquals(
      """
        {
          "lastOffsetRecords": []
        }
        """,
      responsePayload,
      JSONCompareMode.STRICT
    );
  }

}
