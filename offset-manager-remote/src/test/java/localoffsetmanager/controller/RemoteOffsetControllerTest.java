package localoffsetmanager.controller;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RemoteOffsetControllerTest extends ControllerTestSupport {

  @Test
  void getLastOffsetRecord() throws Exception {
    String responsePayload = mockMvc.perform(
      MockMvcRequestBuilders.get("/v1/offset-records")
        .param("key", "notExistKey")
    )
      .andExpect(status().isNotFound())
      .andReturn()
      .getResponse()
      .getContentAsString();

    JSONAssert.assertEquals(
      responsePayload,
      """
        {
          "key": "notExistKey",
          "message": "Not Found Last Offset Record by key: notExistKey",
        }
        """,
      JSONCompareMode.STRICT
    );

  }

  @Test
  void getLastOffsetRecords() {
  }
}
