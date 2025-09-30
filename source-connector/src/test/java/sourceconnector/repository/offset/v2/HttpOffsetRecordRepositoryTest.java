package sourceconnector.repository.offset.v2;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import offsetmanager.domain.DefaultOffsetRecord;
import offsetmanager.domain.OffsetRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

class HttpOffsetRecordRepositoryTest {
    @DisplayName("Should get last offset record when exists")
    @Test
    void findLastOffsetRecordTest() {
      try (MockWebServer server = new MockWebServer()) {
        // given
        server.enqueue(new MockResponse
          .Builder()
          .body("""
            {
              "key": "test-key1",
              "offset": 100
            }
            """)
          .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
          .code(OK.getStatusCode())
          .build());
        server.start();
        String baseUrl = server.url("/v1/offset-records").toString();

        OffsetRecordRepository repository = new HttpOffsetRecordRepository(baseUrl);
        // when
        Optional<OffsetRecord> offsetRecord = repository.findLastOffsetRecord("test-key1");

        // then
        assertThat(offsetRecord)
          .isPresent()
          .get()
          .isEqualTo(new DefaultOffsetRecord("test-key1", 100L));

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @DisplayName("Should get empty when last offset record not exists")
    @Test
    void failedToGetRecordTest() {
      try (MockWebServer server = new MockWebServer()) {
        // given
        server.enqueue(new MockResponse
          .Builder()
          .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
          .code(Response.Status.NOT_FOUND.getStatusCode())
          .build());
        server.start();
        String baseUrl = server.url("/v1/offset-records").toString();

        OffsetRecordRepository repository = new HttpOffsetRecordRepository(baseUrl);
        // when
        Optional<OffsetRecord> offsetRecord = repository.findLastOffsetRecord("not-exists-key1");
        // then
        assertThat(offsetRecord).isEmpty();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @DisplayName("Should get last offset record list when exists")
    @Test
    void findLastOffsetRecordsTest() {
      try (MockWebServer server = new MockWebServer()) {
        // given
        server.enqueue(new MockResponse
          .Builder()
          .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
          .code(OK.getStatusCode())
          .body("""
            {
              "lastOffsetRecords": [
                {
                  "key": "test-key2",
                  "offset": 200
                },
                {
                  "key": "test-key3",
                  "offset": 300
                }
              ]
            }
          """)
          .build()
        );

        server.start();
        String baseUrl = server.url("/v1/offset-records").toString();
        OffsetRecordRepository repository = new HttpOffsetRecordRepository(baseUrl);
        // when
        List<OffsetRecord> offsetRecords = repository.findLastOffsetRecords(List.of(
          "test-key2",
          "test-key3"
        ));

        assertThat(offsetRecords)
          .containsExactlyInAnyOrder(
            new DefaultOffsetRecord("test-key2", 200L),
            new DefaultOffsetRecord("test-key3", 300L)
          );
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }

    @DisplayName("Should get empty list when last offset record not exists")
    @Test
    void failedToGetRecordListTest() {
      try(MockWebServer server = new MockWebServer()) {
        // given
        server.enqueue(new MockResponse
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
        server.start();
        String baseUrl = server.url("/v1/offset-records").toString();
        OffsetRecordRepository repository = new HttpOffsetRecordRepository(baseUrl);

        // when
        List<OffsetRecord> offsetRecord = repository.findLastOffsetRecords(List.of(
          "not-exists-key2",
          "not-exists-key3"
        ));
        // then
        assertThat(offsetRecord)
          .hasSize(0)
          .isEmpty();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

}
