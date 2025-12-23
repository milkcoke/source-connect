package sourceconnector.repository.offset;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.file.factory.FileKeyParser;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetRecord;
import offsetmanager.service.dto.LastOffsetRecordBatchResponse;
import offsetmanager.service.dto.LastOffsetRecordResponse;
import sourceconnector.service.offset.OffsetRecordRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.OK;

@Slf4j
@RequiredArgsConstructor
public class HttpOffsetRecordRepository implements OffsetRecordRepository {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final String baseUrl;

    @Override
    public Optional<OffsetRecord> findLastOffsetRecord(FileKey key) {
        URI url = URI.create(baseUrl).resolve("/api/offset-records?key=" + key.get());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .header("X-API-Version", "v1")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int responseStatus = response.statusCode();
            if (responseStatus == OK.getStatusCode()) {
                LastOffsetRecordResponse offsetRecord = objectMapper.readValue(
                    response.body(),
                    LastOffsetRecordResponse.class
                );
                return Optional.of(new DefaultOffsetRecord(
                  FileKeyParser.parse(offsetRecord.key()),
                  offsetRecord.offset())
                );
            } else if (responseStatus == NOT_FOUND.getStatusCode()) {
                return Optional.empty();
            } else {
                throw new RuntimeException("Failed to fetch offset record, status code: " + responseStatus);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch offset record", e);
        }
    }

    @Override
    public List<OffsetRecord> findLastOffsetRecords(List<FileKey> keys) {
        URI url = URI.create(baseUrl).resolve("/api/offset-records");
        List<String> fileKeys= keys.stream().map(FileKey::get).toList();
        String requestBody = objectMapper.writeValueAsString(Map.of("keys", fileKeys));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .headers(
                  HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON,
                  "X-API-Version", "v1"
                )
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == OK.getStatusCode()) {
                LastOffsetRecordBatchResponse batchResponse = objectMapper.readValue(
                  response.body(),
                  LastOffsetRecordBatchResponse.class
                );
                return batchResponse.lastOffsetRecords()
                        .stream()
                        .map(lastOffsetRecord -> new DefaultOffsetRecord(
                          FileKeyParser.parse(lastOffsetRecord.key()),
                          lastOffsetRecord.offset())
                        )
                        .collect(Collectors.toList());
            }

        } catch (IOException | InterruptedException ex) {
            log.error("Exception occurred while fetching offset records", ex);
        }

        return Collections.emptyList();
    }

  @Override
  public void close() throws Exception {
    this.httpClient.close();
  }
}
