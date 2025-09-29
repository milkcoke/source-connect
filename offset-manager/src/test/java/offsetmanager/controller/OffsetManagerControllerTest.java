package offsetmanager.controller;

import offsetmanager.exception.OffsetNotFoundException;
import offsetmanager.service.dto.LastOffsetRecordBatchResponse;
import offsetmanager.service.dto.LastOffsetRecordResponse;
import offsetmanager.domain.DefaultOffsetRecord;
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
    when(offsetManagerService.readLastOffset("lastKey"))
      .thenReturn(LastOffsetRecordResponse.from(new DefaultOffsetRecord("lastKey", 5L)));

    // when
    String responsePayload = mockMvc.perform(
      MockMvcRequestBuilders.get("/v1/offset-records")
        .param("key", "lastKey")
    ).andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString();

    // then
    JSONAssert.assertEquals(
      """
        {
          "key": "lastKey",
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
    when(offsetManagerService.readLastOffsets(List.of("key1", "key2", "key3")))
      .thenReturn(
        LastOffsetRecordBatchResponse.from(List.of(
          new DefaultOffsetRecord("key1", 5L),
          new DefaultOffsetRecord("key2", 3L),
          new DefaultOffsetRecord("key3", -1L)
        ))
      );

    // when
    String responsePayload = mockMvc.perform(
      MockMvcRequestBuilders.post("/v1/offset-records")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "keys": ["key1", "key2", "key3"]
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
              "key": "key1",
              "offset": 5
            },
            {
              "key": "key2",
              "offset": 3
            },
            {
              "key": "key3",
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
    when(offsetManagerService.readLastOffsets(List.of("key1", "key2", "key3")))
      .thenReturn(
        LastOffsetRecordBatchResponse.from(Collections.emptyList())
      );

    // when
    String responsePayload = mockMvc.perform(
      MockMvcRequestBuilders.post("/v1/offset-records")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          {
            "keys": ["key1", "key2", "key3"]
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
