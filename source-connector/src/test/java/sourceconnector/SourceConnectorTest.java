package sourceconnector;

import offsetmanager.domain.OffsetStatus;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.log.LogMetadata;
import sourceconnector.domain.offset.LocalFileOffsetRecord;
import sourceconnector.domain.factory.JSONLogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.service.batcher.Batchable;
import sourceconnector.service.batcher.LogBatcher;
import sourceconnector.service.pipeline.FileBaseLogPipeline;
import sourceconnector.service.pipeline.Pipeline;
import sourceconnector.service.processor.impl.EmptyFilterProcessor;
import sourceconnector.service.processor.impl.TrimMapperProcessor;
import sourceconnector.service.producer.BatchProduceService;
import sourceconnector.service.producer.BatchProducer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Disabled
class SourceConnectorTest {
  private static final Properties props = new Properties();
  static {
    props.putAll(Map.of(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        ACKS_CONFIG, "-1",
        COMPRESSION_TYPE_CONFIG, CompressionType.LZ4.name,
        KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class,
        LINGER_MS_CONFIG, 100,
        BATCH_SIZE_CONFIG, 524288,
        ENABLE_IDEMPOTENCE_CONFIG, true,
        TRANSACTIONAL_ID_CONFIG, "test-s3"
      )
    );
  }

  @DisplayName("Send batch")
  @Test
  void mainTest() {

    File file = Path.of("src/test/resources/sample-data/large-ndjson.ndjson").toFile();

    Pipeline<Log> pipeline = FileBaseLogPipeline.create(
      new LocalFileRepository(),
      file.getPath(),
      new JSONLogFactory(),
      new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor()
    );

    Batchable<Log> batcher = new LogBatcher(pipeline, 10_000);
    BatchProducer<String> producer = new BatchProduceService(props, "log", "local-offset");

    // when
    List<Log> messages;
    LogMetadata lastMessageMetadata = LogMetadata.EMPTY;
    while((messages = batcher.nextBatch().get()) != Collections.EMPTY_LIST) {lastMessageMetadata = messages.getLast().getMetadata();
      List<String> messageBatch = messages
        .stream()
        .map(Log::get)
        .toList();
      producer.sendBatch(
        new LocalFileOffsetRecord(
          lastMessageMetadata.key(),
          lastMessageMetadata.offset()
        ),
        ()-> messageBatch
      );
    }

    if (lastMessageMetadata != LogMetadata.EMPTY) {
      producer.sendBatch(new LocalFileOffsetRecord(
        lastMessageMetadata.key(),
        OffsetStatus.COMPLETED.getValue()
      ), Collections::emptyList);
    }

  }

  @DisplayName("Should handle multiple files")
  @Test
  void handleDirectory() throws IOException {
    // given
    BatchProducer<String> producer = new BatchProduceService(props, "log", "local-offset");

    // when
    try (var stream = Files.walk(Paths.get("src/test/resources/sample-data"))) {
      List<File> files = stream
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".ndjson"))
        .map(Path::toFile)
        .toList();

      // then
      for (File file : files) {
        Pipeline<Log> pipeline = FileBaseLogPipeline.create(
          new LocalFileRepository(),
          file.getPath(),
          new JSONLogFactory(),
          new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor()
        );

        Batchable<Log> batcher = new LogBatcher(pipeline, 10_000);

        List<Log> messages;
        LogMetadata lastMessageMetadata = LogMetadata.EMPTY;
        while((messages = batcher.nextBatch().get()) != Collections.EMPTY_LIST) {
          lastMessageMetadata = messages.getLast().getMetadata();
          List<String> messageBatch = messages
            .stream()
            .map(Log::get)
            .toList();
          producer.sendBatch(
            new LocalFileOffsetRecord(
              lastMessageMetadata.key(),
              lastMessageMetadata.offset()
            ),
            ()-> messageBatch
          );
        }

        if (lastMessageMetadata != LogMetadata.EMPTY) {
          producer.sendBatch(new LocalFileOffsetRecord(
            lastMessageMetadata.key(),
            OffsetStatus.COMPLETED.getValue()
          ), Collections::emptyList);
        }

      }
    }
  }

  @DisplayName("Nothing to produce after completes")
  @Test
  void NothingToDoAfterProcessingAllFiles() throws IOException {
    // given
    BatchProducer<String> producer = new BatchProduceService(props, "log", "local-offset");
    // when
    try (var stream = Files.walk(Paths.get("src/test/resources/sample-data"))) {
      List<File> files = stream
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".ndjson"))
        .map(Path::toFile)
        .toList();

      // then
      for (File file : files) {
        Pipeline<Log> pipeline = FileBaseLogPipeline.create(
          new LocalFileRepository(),
          file.getPath(),
          new JSONLogFactory(),
          new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor()
        );

        Batchable<Log> batcher = new LogBatcher(pipeline, 10_000);

        List<Log> messages;
        LogMetadata lastMessageMetadata = LogMetadata.EMPTY;
        while((messages = batcher.nextBatch().get()) != Collections.EMPTY_LIST) {
          lastMessageMetadata = messages.getLast().getMetadata();
          List<String> messageBatch = messages
            .stream()
            .map(Log::get)
            .toList();
          producer.sendBatch(
            new LocalFileOffsetRecord(
              lastMessageMetadata.key(),
              lastMessageMetadata.offset()
            ),
            ()-> messageBatch
          );
        }

        if (lastMessageMetadata != LogMetadata.EMPTY) {
          producer.sendBatch(new LocalFileOffsetRecord(
            lastMessageMetadata.key(),
            OffsetStatus.COMPLETED.getValue()
          ), Collections::emptyList);
        }

      }
    }
  }
}
