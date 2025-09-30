package sourceconnector.repository.offset.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.DefaultOffsetRecord;
import offsetmanager.domain.OffsetRecord;
import offsetmanager.service.dto.LastOffsetRecordBatchResponse;
import offsetmanager.service.dto.LastOffsetRecordResponse;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.OK;

@Slf4j
@RequiredArgsConstructor
public class HttpOffsetRecordRepository implements OffsetRecordRepository {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;

    @Override
    public Optional<OffsetRecord> findLastOffsetRecord(String key) {
        String url = String.format("%s/v1/offset-records?key=%s", baseUrl, URLEncoder.encode(key, StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int responseStatus = response.statusCode();
            if (responseStatus == OK.getStatusCode()) {
                LastOffsetRecordResponse offsetRecord = objectMapper.readValue(
                        response.body(),
                        LastOffsetRecordResponse.class
                );
                return Optional.of(new DefaultOffsetRecord(offsetRecord.key(), offsetRecord.offset()));
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
    public List<OffsetRecord> findLastOffsetRecords(List<String> keys) throws JsonProcessingException {
        String url = String.format("%s/v1/offset-records", baseUrl);
        String requestBody = objectMapper.writeValueAsString(Collections.singletonList(keys));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == OK.getStatusCode()) {
                LastOffsetRecordBatchResponse batchResponse = objectMapper.readValue(response.body(), LastOffsetRecordBatchResponse.class);
                return batchResponse.lastOffsetRecords()
                        .stream()
                        .map(lastOffsetRecord -> new DefaultOffsetRecord(lastOffsetRecord.key(), lastOffsetRecord.offset()))
                        .collect(Collectors.toList());
            }

        } catch (IOException | InterruptedException ex) {
            log.error("Exception occurred while fetching offset records", ex);
        }

        return Collections.emptyList();
    }
}
