package offsetmanager.controller

import offsetmanager.api.v1.dto.LastOffsetRecordBatchResponse
import offsetmanager.api.v1.dto.LastOffsetRecordResponse
import offsetmanager.domain.file.factory.FileKeyParser.Companion.parse
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.exception.OffsetManagerNotReadyException
import offsetmanager.exception.OffsetNotFoundException
import org.json.JSONException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import org.springframework.web.client.ApiVersionInserter
import org.springframework.web.util.UriBuilder

internal class OffsetManagerControllerTest : ControllerTestSupport() {
  private lateinit var client: RestTestClient

  @BeforeEach
  fun setUp() {
    client = RestTestClient.bindTo(mockMvc)
      .apiVersionInserter(ApiVersionInserter.useHeader("X-API-Version"))
      .defaultApiVersion("v1")
      .build()
  }

  @DisplayName("Should throw 404 Not Found when the offset record does not exist")
  @Test
  @Throws(JSONException::class)
  fun offsetNotFoundTest() {
    // given
    Mockito.`when`<LastOffsetRecordResponse>(offsetManagerService.readLastOffset("notExistKey"))
      .thenThrow(OffsetNotFoundException("notExistKey"))

    val responseBody = client.get()
      .uri("/api/offset-records?key=notExistKey")
      .exchange()
      .expectStatus().isNotFound()
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody<String>()
      .returnResult()
      .getResponseBody()

    // then
    JSONAssert.assertEquals(
      """
        {
          "statusCode": 404,
          "type": "OFFSET_NOT_FOUND",
          "message": "Offset not found for key: notExistKey"
        }
        """.trimIndent(),
      responseBody,
      JSONCompareMode.STRICT
    )
  }

  @DisplayName("Should return 400 Bad Request when the key parameter is invalid")
  @Test
  @Throws(JSONException::class)
  fun invalidKeyRequestTest() {
    // given
    val responsePayload = client.get()
      .uri("/api/offset-records?key=a")
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody<String>()
      .returnResult()
      .getResponseBody()

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
        """.trimIndent(),
      responsePayload,
      JSONCompareMode.STRICT
    )
  }

  @DisplayName("Should get last offset record successfully")
  @Test
  @Throws(JSONException::class)
  fun lastOffsetReturnTest() {
    // given
    Mockito.`when`<LastOffsetRecordResponse>(offsetManagerService.readLastOffset("s3://my-bucket/test.ndjson"))
      .thenReturn(
        LastOffsetRecordResponse.from(
          DefaultOffsetRecord(parse("s3://my-bucket/test.ndjson"), 5L)
        )
      )

    // when
    val responsePayload = client.get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder.path("/api/offset-records")
          .queryParam("key", "s3://my-bucket/test.ndjson")
          .build()
      }
      .exchange()
      .expectStatus().isOk()
      .expectBody<String>()
      .returnResult()
      .getResponseBody()
    // then
    JSONAssert.assertEquals(
      """
        {
          "key": "s3://my-bucket/test.ndjson",
          "offset": 5
        }
        """.trimIndent(),
      responsePayload,
      JSONCompareMode.STRICT
    )
  }

  @DisplayName("Should get each last offset record successfully in batch")
  @Test
  @Throws(JSONException::class)
  fun batchLastOffsetReturnTest() {
    // given
    Mockito.`when`<LastOffsetRecordBatchResponse>(
      offsetManagerService.readLastOffsets(
        listOf(
          "s3://my-bucket/key1.txt",
          "s3://my-bucket/key2.txt",
          "s3://my-bucket/key3.txt"
        )
      )
    )
      .thenReturn(
        LastOffsetRecordBatchResponse.from(
          listOf(
            DefaultOffsetRecord(parse("s3://my-bucket/key1.txt"), 5L),
            DefaultOffsetRecord(parse("s3://my-bucket/key2.txt"), 3L),
            DefaultOffsetRecord(parse("s3://my-bucket/key3.txt"), -1L)
          )
        )
      )

    // when
    val responsePayload = client.post()
      .uri("/api/offset-records")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        """
          {
            "keys": ["s3://my-bucket/key1.txt", "s3://my-bucket/key2.txt", "s3://my-bucket/key3.txt"]
          }
          """.trimIndent()
      )
      .exchange()
      .expectStatus().isOk()
      .expectBody<String>()
      .returnResult()
      .getResponseBody()

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
        """.trimIndent(),
      responsePayload,
      JSONCompareMode.STRICT
    )
  }

  @DisplayName("Return status 200 when request is valid even though retrieved list is empty")
  @Test
  @Throws(JSONException::class)
  fun batchResponseEmptyTest() {
    // given
    Mockito.`when`<LastOffsetRecordBatchResponse>(
      offsetManagerService.readLastOffsets(
        listOf(
          "s3://my-bucket/file1.txt",
          "s3://my-bucket/file2.txt",
          "s3://my-bucket/file3.txt"
        )
      )
    )
      .thenReturn(
        LastOffsetRecordBatchResponse.from(listOf())
      )

    // when
    val responsePayload = client.post()
      .uri("/api/offset-records")
      .contentType(MediaType.APPLICATION_JSON)
      .body("""
          {
            "keys": ["s3://my-bucket/file1.txt", "s3://my-bucket/file2.txt", "s3://my-bucket/file3.txt"]
          }
        """.trimIndent()
      )
      .exchange()
      .expectBody<String>()
      .returnResult()
      .getResponseBody()

    // then
    JSONAssert.assertEquals(
      """
        {
          "lastOffsetRecords": []
        }
        """.trimIndent(),
      responsePayload,
      JSONCompareMode.STRICT
    )
  }


  @DisplayName("Return status 503 when OffsetManager is not available")
  @Test
  @Throws(JSONException::class)
  fun offsetManagerUnAvailableTest() {
    // given
    Mockito.`when`<LastOffsetRecordResponse>(offsetManagerService.readLastOffset("s3://my-bucket/file1.txt"))
      .thenThrow(OffsetManagerNotReadyException())

    // when
    val responsePayload = client.get()
      .uri { uriBuilder: UriBuilder ->
        uriBuilder.path("/api/offset-records")
          .queryParam("key", "s3://my-bucket/file1.txt")
          .build()
      }
      .exchange()
      .expectBody<String>()
      .returnResult()
      .getResponseBody()

    // then
    JSONAssert.assertEquals(
      """
        {
          "statusCode": 503,
          "type": "OFFSET_MANAGER_NOT_READY",
          "message": "Offset storage is still initializing. Please retry later."
        }
        """.trimIndent(),
      responsePayload,
      JSONCompareMode.STRICT
    )
  }
}
