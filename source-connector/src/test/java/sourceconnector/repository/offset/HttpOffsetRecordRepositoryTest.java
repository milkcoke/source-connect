package sourceconnector.repository.offset;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetRecord;
import org.junit.jupiter.api.*;
import sourceconnector.service.offset.OffsetRecordRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HttpOffsetRecordRepositoryTest {
  private final MockWebServer mockWebServer = new MockWebServer();

  @BeforeAll
  void setup() throws IOException {
    mockWebServer.start();
  }

  @AfterAll
  void teardown() {
    mockWebServer.close();
  }

  @DisplayName("Should get last offset record when exists")
  @Test
  void findLastOffsetRecordTest() {
    // given
    mockWebServer.enqueue(new MockResponse
      .Builder()
      .body("""
        {
          "key": "file:///test-key1",
          "offset": 100
        }
        """)
      .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
      .code(OK.getStatusCode())
      .build());
    String baseUrl = mockWebServer.url("/").toString();

    OffsetRecordRepository repository = new HttpOffsetRecordRepository(baseUrl);
    FileKey fileKey = FileKeyParser.parse("file:///test-key1");
    // when
    Optional<OffsetRecord> offsetRecord = repository.findLastOffsetRecord(fileKey);

    // then
    assertThat(offsetRecord)
      .isPresent()
      .get()
      .isEqualTo(new DefaultOffsetRecord(
        fileKey,
        100L
      ));

  }

  @DisplayName("Should get empty when last offset record not exists")
  @Test
  void failedToGetRecordTest() {
    // given
    mockWebServer.enqueue(new MockResponse
      .Builder()
      .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
      .code(Response.Status.NOT_FOUND.getStatusCode())
      .build());
    String baseUrl = mockWebServer.url("/").toString();

    OffsetRecordRepository repository = new HttpOffsetRecordRepository(baseUrl);
    FileKey notExistFileKey = FileKeyParser.parse("file:///not-exist-key1");
    // when
    Optional<OffsetRecord> offsetRecord = repository.findLastOffsetRecord(notExistFileKey);
    // then
    assertThat(offsetRecord).isEmpty();
  }

  @DisplayName("Should get last offset record list when exists")
  @Test
  void findLastOffsetRecordsTest() {
    // given
    mockWebServer.enqueue(new MockResponse
      .Builder()
      .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
      .code(OK.getStatusCode())
      .body("""
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
      """)
      .build()
    );

    String baseUrl = mockWebServer.url("/").toString();
    OffsetRecordRepository repository = new HttpOffsetRecordRepository(baseUrl);
    List<FileKey> fileKeys = List.of(
      FileKeyParser.parse("file:///test-key2"),
      FileKeyParser.parse("file:///test-key3")
    );

    // when
    List<OffsetRecord> offsetRecords = repository.findLastOffsetRecords(fileKeys);

    assertThat(offsetRecords)
      .containsExactlyInAnyOrder(
        new DefaultOffsetRecord(FileKeyParser.parse("file:///test-key2"), 200L),
        new DefaultOffsetRecord(FileKeyParser.parse("file:///test-key3"), 300L)
      );

  }

  @DisplayName("Should get empty list when last offset record not exists")
  @Test
  void failedToGetRecordListTest() {
    // given
    mockWebServer.enqueue(new MockResponse
      .Builder()
      .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
      .code(OK.getStatusCode())
      .body("""
        {
          "lastOffsetRecords": []
        }
      """)
      .build()
    );
    String baseUrl = mockWebServer.url("/").toString();
    OffsetRecordRepository repository = new HttpOffsetRecordRepository(baseUrl);
    List<FileKey> notExistFileKeys = List.of(
      FileKeyParser.parse("file:///not-exists-key2"),
      FileKeyParser.parse("file:///not-exists-key3")
    );
    // when
    List<OffsetRecord> offsetRecord = repository.findLastOffsetRecords(notExistFileKeys);
    // then
    assertThat(offsetRecord)
      .hasSize(0)
      .isEmpty();
  }

}
