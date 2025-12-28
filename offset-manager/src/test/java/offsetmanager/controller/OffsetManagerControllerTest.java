package offsetmanager.controller;

import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.exception.OffsetManagerNotReadyException;
import offsetmanager.exception.OffsetNotFoundException;
import offsetmanager.api.v1.dto.LastOffsetRecordBatchResponse;
import offsetmanager.api.v1.dto.LastOffsetRecordResponse;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.ApiVersionInserter;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

class OffsetManagerControllerTest extends ControllerTestSupport {

  private RestTestClient client;

  @BeforeEach
  void setUp() {
    client = RestTestClient.bindTo(mockMvc)
      .apiVersionInserter(ApiVersionInserter.useHeader("X-API-Version"))
      .defaultApiVersion("v1")
      .build();
  }

  @DisplayName("Should throw 404 Not Found when the offset record does not exist")
  @Test
  void OffsetNotFoundTest() throws JSONException {
    // given
    when(offsetManagerService.readLastOffset("notExistKey"))
      .thenThrow(new OffsetNotFoundException("notExistKey"));

    String responseBody = client.get()
      .uri("/api/offset-records?key=notExistKey")
      .exchange()
      .expectStatus().isNotFound()
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(String.class)
      .returnResult()
      .getResponseBody();

    // then
    JSONAssert.assertEquals(
"""
        {
          "statusCode": 404,
          "type": "OFFSET_NOT_FOUND",
          "message": "Offset not found for key: notExistKey"
        }
        """,
      responseBody,
      JSONCompareMode.STRICT
    );

  }

  @DisplayName("Should return 400 Bad Request when the key parameter is invalid")
  @Test
  void invalidKeyRequestTest() throws  JSONException{
    // given
    String responsePayload = client.get()
      .uri("/api/offset-records?key=a")
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody(String.class)
      .returnResult()
      .getResponseBody();

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
  void lastOffsetReturnTest() throws JSONException {
    // given
    when(offsetManagerService.readLastOffset("s3://my-bucket/test.ndjson"))
      .thenReturn(LastOffsetRecordResponse.from(
        new DefaultOffsetRecord(FileKeyParser.parse("s3://my-bucket/test.ndjson"), 5L))
      );

    // when
    String responsePayload = client.get()
      .uri(uriBuilder ->
        uriBuilder.path("/api/offset-records")
          .queryParam("key", "s3://my-bucket/test.ndjson")
          .build())
      .exchange()
      .expectStatus().isOk()
      .expectBody(String.class)
      .returnResult()
      .getResponseBody();
    // then
    JSONAssert.assertEquals(
      """
        {
          "key": "s3://my-bucket/test.ndjson",
          "offset": 5
        }
        """,
      responsePayload,
      JSONCompareMode.STRICT
    );
  }

  @DisplayName("Should get each last offset record successfully in batch")
  @Test
  void BatchLastOffsetReturnTest() throws JSONException {
    // given
    when(offsetManagerService.readLastOffsets(List.of("s3://my-bucket/key1.txt", "s3://my-bucket/key2.txt", "s3://my-bucket/key3.txt")))
      .thenReturn(
        LastOffsetRecordBatchResponse.from(List.of(
          new DefaultOffsetRecord(FileKeyParser.parse("s3://my-bucket/key1.txt"), 5L),
          new DefaultOffsetRecord(FileKeyParser.parse("s3://my-bucket/key2.txt"), 3L),
          new DefaultOffsetRecord(FileKeyParser.parse("s3://my-bucket/key3.txt"), -1L)
        ))
      );

    // when
    String responsePayload = client.post()
      .uri("/api/offset-records")
      .contentType(MediaType.APPLICATION_JSON)
          .body("""
          {
            "keys": ["s3://my-bucket/key1.txt", "s3://my-bucket/key2.txt", "s3://my-bucket/key3.txt"]
          }
          """)
      .exchange()
      .expectStatus().isOk()
      .expectBody(String.class)
      .returnResult()
      .getResponseBody();

    JSONAssert.assertEquals(
      """
        {
          "lastOffsetRecords": [
            {
              "key": "s3://my-bucket/key1.txt",
              "offset": 5
            },
            {
              "key": "s3://my-bucket/key2.txt",
              "offset": 3
            },
            {
              "key": "s3://my-bucket/key3.txt",
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
  void batchResponseEmptyTest() throws JSONException {
    // given
    when(offsetManagerService.readLastOffsets(List.of(
      "s3://my-bucket/file1.txt",
      "s3://my-bucket/file2.txt",
      "s3://my-bucket/file3.txt"))
    )
      .thenReturn(
        LastOffsetRecordBatchResponse.from(Collections.emptyList())
      );

    // when
    String responsePayload = client.post()
        .uri("/api/offset-records")
        .contentType(MediaType.APPLICATION_JSON)
        .body("""
          {
            "keys": ["s3://my-bucket/file1.txt", "s3://my-bucket/file2.txt", "s3://my-bucket/file3.txt"]
          }
        """)
          .exchange()
          .expectBody(String.class)
          .returnResult()
          .getResponseBody();

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


  @DisplayName("Return status 503 when OffsetManager is not available")
  @Test
  void offsetManagerUnAvailableTest() throws JSONException {
    // given
    when(offsetManagerService.readLastOffset("s3://my-bucket/file1.txt"))
      .thenThrow(new OffsetManagerNotReadyException());

    // when
    String responsePayload = client.get()
      .uri(uriBuilder ->
        uriBuilder.path("/api/offset-records")
          .queryParam("key", "s3://my-bucket/file1.txt")
          .build())
      .exchange()
      .expectBody(String.class)
      .returnResult()
      .getResponseBody();

    // then
    JSONAssert.assertEquals(
      """
        {
          "statusCode": 503,
          "type": "OFFSET_MANAGER_NOT_READY",
          "message": "Offset storage is still initializing. Please retry later."
        }
        """,
      responsePayload,
      JSONCompareMode.STRICT
    );
  }

}
