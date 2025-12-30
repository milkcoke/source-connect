package sourceconnector.repository.offset

import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.factory.FileKeyParser.Companion.parse
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.IOException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HttpOffsetRecordRepositoryTest {
  private val mockWebServer = MockWebServer()

  @BeforeAll
  @Throws(IOException::class)
  fun setup() {
    mockWebServer.start()
  }

  @AfterAll
  fun teardown() {
    mockWebServer.close()
  }

  @DisplayName("Should get last offset record when exists")
  @Test
  @Throws(Exception::class)
  fun findLastOffsetRecordTest() {
    // given
    mockWebServer.enqueue(
      MockResponse.Builder()
        .body(
          """
        {
          "key": "file:///test-key1",
          "offset": 100
        }
        """.trimIndent()
        )
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .code(Response.Status.OK.statusCode)
        .build()
    )
    val baseUrl = mockWebServer.url("/").toString()

    HttpOffsetRecordRepository(baseUrl).use { repository ->
      val fileKey = parse("file:///test-key1")
      // when
      val offsetRecord: OffsetRecord? = repository.findLastOffsetRecord(fileKey)

      // then
      assertThat<OffsetRecord?>(offsetRecord).isEqualTo(
        DefaultOffsetRecord(fileKey, 100L)
      )
    }
  }

  @DisplayName("Should get null when last offset record not exists")
  @Test
  @Throws(Exception::class)
  fun failedToGetRecordTest() {
    // given
    mockWebServer.enqueue(
      MockResponse.Builder()
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .code(Response.Status.NOT_FOUND.statusCode)
        .build()
    )
    val baseUrl = mockWebServer.url("/").toString()

    HttpOffsetRecordRepository(baseUrl).use { repository ->
      val notExistFileKey = parse("file:///not-exist-key1")
      // when
      val offsetRecord: OffsetRecord? = repository.findLastOffsetRecord(notExistFileKey)
      // then
      assertThat<OffsetRecord?>(offsetRecord).isNull()
    }
  }

  @DisplayName("Should get last offset record list when exists")
  @Test
  @Throws(Exception::class)
  fun findLastOffsetRecordsTest() {
    // given
    mockWebServer.enqueue(
      MockResponse.Builder()
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .code(Response.Status.OK.statusCode)
        .body(
          """
        {
          "lastOffsetRecords": [
            {
              "key": "file:///test-key2",
              "offset": 200
            },
            {
              "key": "file:///test-key3",
              "offset": 300
            }
          ]
        }
      
      """.trimIndent()
        )
        .build()
    )

    val baseUrl = mockWebServer.url("/").toString()
    HttpOffsetRecordRepository(baseUrl).use { repository ->
      val fileKeys = listOf(
        parse("file:///test-key2"),
        parse("file:///test-key3")
      )
      // when
      val offsetRecords: List<OffsetRecord> = repository.findLastOffsetRecords(fileKeys)
      assertThat<OffsetRecord?>(offsetRecords)
        .containsExactlyInAnyOrder(
          DefaultOffsetRecord(parse("file:///test-key2"), 200L),
          DefaultOffsetRecord(parse("file:///test-key3"), 300L)
        )
    }
  }

  @DisplayName("Should get empty list when last offset record not exists")
  @Test
  @Throws(Exception::class)
  fun failedToGetRecordListTest() {
    // given
    mockWebServer.enqueue(
      MockResponse.Builder()
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .code(Response.Status.OK.statusCode)
        .body(
          """
        {
          "lastOffsetRecords": []
        }
      """.trimIndent()
        )
        .build()
    )
    val baseUrl = mockWebServer.url("/").toString()
    HttpOffsetRecordRepository(baseUrl).use { repository ->
      val notExistFileKeys = listOf<FileKey>(
        parse("file:///not-exists-key2"),
        parse("file:///not-exists-key3")
      )
      // when
      val offsetRecord: List<OffsetRecord> = repository.findLastOffsetRecords(notExistFileKeys)
      // then
      assertThat<OffsetRecord>(offsetRecord)
        .hasSize(0)
        .isEmpty()
    }
  }
}
