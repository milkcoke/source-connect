package sourceconnector.repository.offset

import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import offsetmanager.api.v1.dto.LastOffsetRecordBatchResponse
import offsetmanager.api.v1.dto.LastOffsetRecordResponse
import offsetmanager.domain.file.FileKey
import offsetmanager.domain.file.factory.FileKeyParser.Companion.parse
import offsetmanager.domain.offset.DefaultOffsetRecord
import offsetmanager.domain.offset.OffsetRecord
import sourceconnector.service.offset.OffsetRecordRepository
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.stream.Collectors

class HttpOffsetRecordRepository(
  private val baseUrl: String
) : OffsetRecordRepository {
  private val httpClient: HttpClient = HttpClient.newHttpClient()
  private val objectMapper: ObjectMapper = JsonMapper.builder()
    .addModule(KotlinModule.Builder().build())
    .build()

  override fun findLastOffsetRecord(key: FileKey): OffsetRecord? {
    val url = URI.create(baseUrl).resolve("/api/offset-records?key=" + key.get())
    val request = HttpRequest.newBuilder()
      .uri(url)
      .GET()
      .header("X-API-Version", "v1")
      .build()
    try {
      val response = httpClient.send<String>(request, HttpResponse.BodyHandlers.ofString())
      val responseStatus = response.statusCode()
      if (responseStatus == Response.Status.OK.statusCode) {
        val offsetRecord = objectMapper.readValue<LastOffsetRecordResponse>(
          response.body(),
          LastOffsetRecordResponse::class.java
        )
        return DefaultOffsetRecord(parse(offsetRecord.key), offsetRecord.offset)

      } else if (responseStatus == Response.Status.NOT_FOUND.statusCode) {
        return null
      } else {
        throw RuntimeException("Failed to fetch offset record, status code: $responseStatus")
      }
    } catch (e: IOException) {
      throw IllegalStateException("Failed to fetch offset records from OffsetManager", e)
    } catch (e: InterruptedException) {
      throw IllegalStateException("Failed to fetch offset records from OffsetManager", e)
    }
  }

  override fun findLastOffsetRecords(keys: List<FileKey>): List<OffsetRecord> {
    val url = URI.create(baseUrl).resolve("/api/offset-records")
    val fileKeys: List<String> = keys.map { fileKey -> fileKey.get() }.toList()
    val requestBody = objectMapper.writeValueAsString(mapOf("keys" to fileKeys))
    val request = HttpRequest.newBuilder()
      .uri(url)
      .headers(
        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON,
        "X-API-Version", "v1"
      )
      .POST(HttpRequest.BodyPublishers.ofString(requestBody))
      .build()
    try {
      val response = httpClient.send<String?>(request, HttpResponse.BodyHandlers.ofString())
      if (response.statusCode() == Response.Status.OK.statusCode) {
        val batchResponse = objectMapper.readValue<LastOffsetRecordBatchResponse>(
          response.body(),
          LastOffsetRecordBatchResponse::class.java
        )
        return batchResponse.lastOffsetRecords
          .stream()
          .map<DefaultOffsetRecord?> { lastOffsetRecord: LastOffsetRecordResponse? ->
            DefaultOffsetRecord(
              parse(lastOffsetRecord!!.key),
              lastOffsetRecord.offset
            )
          }
          .collect(Collectors.toList())
      }
    } catch (ex: IOException) {
      throw IllegalStateException("Failed to fetch offset records from OffsetManager", ex)
    } catch (ex: InterruptedException) {
      throw IllegalStateException("Failed to fetch offset records from OffsetManager", ex)
    }

    return emptyList()
  }

  @Throws(Exception::class)
  override fun close() {
    this.httpClient.close()
  }
}
