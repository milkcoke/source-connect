package localoffsetmanager.controller;

import localoffsetmanager.exception.OffsetNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RemoteOffsetControllerTest extends ControllerTestSupport {

  @DisplayName("Should throw 404 Not Found when the offset record does not exist")
  @Test
  void OffsetNotFoundTest() throws Exception {
    // given
    when(remoteOffsetService.readLastOffset("notExistKey"))
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
      responsePayload,
      """
        {
          "statusCode": 404,
          "type": "OFFSET_NOT_FOUND",
          "message": "Offset not found for key: notExistKey"
        }
        """,
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
      responsePayload,
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
      JSONCompareMode.STRICT
    );
  }

}
