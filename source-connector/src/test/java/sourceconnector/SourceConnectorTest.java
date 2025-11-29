package sourceconnector;

import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetStatus;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import offsetmanager.domain.file.LocalFileKey;
import sourceconnector.domain.log.LogMetadata;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder;
import sourceconnector.domain.pipeline.factory.PipelineBuilder;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.service.batcher.Batchable;
import sourceconnector.service.batcher.LogBatcher;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.processor.impl.EmptyFilterProcessor;
import sourceconnector.domain.processor.impl.TrimMapperProcessor;
import sourceconnector.service.producer.BatchProduceService;
import sourceconnector.service.producer.BatchProducer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

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

    PipelineBuilder<Log> pipelineBuilder = new FileBaseLogPipelineBuilder();
    Path localPath = Path.of("src/test/resources/sample-data/large.ndjson");

    Pipeline<Log> pipeline = pipelineBuilder.create(
      new LocalFileRepository(),
      LocalFileKey.from(localPath),
      new JSONLogFactory(),
      List.of(new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor())
    );

    Batchable<Log> batcher = new LogBatcher(pipeline, 10_000);
    BatchProducer<String> producer = new BatchProduceService(props, "log", "local-offset");

    // when
    LogMetadata lastMessageMetadata = LogMetadata.EMPTY;
    while(batcher.hasNextBatch()) {
      List<Log> messages = batcher.nextBatch().get();
      if (messages.isEmpty()) continue;
      lastMessageMetadata = messages.getLast().getMetadata();
      List<String> messageBatch = messages
        .stream()
        .map(Log::get)
        .toList();
      producer.sendBatch(
        new DefaultOffsetRecord(
          lastMessageMetadata.key(),
          lastMessageMetadata.offset()
        ),
        ()-> messageBatch
      );
    }

    if (lastMessageMetadata != LogMetadata.EMPTY) {
      producer.sendBatch(new DefaultOffsetRecord(
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

    PipelineBuilder<Log> pipelineBuilder = new FileBaseLogPipelineBuilder();
    // when
    try (var stream = Files.walk(Paths.get("src/test/resources/sample-data"))) {
      List<File> files = stream
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".ndjson"))
        .map(Path::toFile)
        .toList();

      // then
      for (File file : files) {
        Pipeline<Log> pipeline = pipelineBuilder.create(
          new LocalFileRepository(),
          LocalFileKey.from(file.toPath()),
          new JSONLogFactory(),
          List.of(new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor())
        );

        Batchable<Log> batcher = new LogBatcher(pipeline, 10_000);

        LogMetadata lastMessageMetadata = LogMetadata.EMPTY;
        while(batcher.hasNextBatch()) {
          List<Log> messages = batcher.nextBatch().get();
          if (messages.isEmpty()) continue;
          lastMessageMetadata = messages.getLast().getMetadata();
          List<String> messageBatch = messages
            .stream()
            .map(Log::get)
            .toList();
          producer.sendBatch(
            new DefaultOffsetRecord(
              lastMessageMetadata.key(),
              lastMessageMetadata.offset()
            ),
            ()-> messageBatch
          );
        }

        if (lastMessageMetadata != LogMetadata.EMPTY) {
          producer.sendBatch(new DefaultOffsetRecord(
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
    BatchProducer<String> producer = new BatchProduceService(props, "log-topic", "local-offset");
    PipelineBuilder<Log> pipelineBuilder = new FileBaseLogPipelineBuilder();
    // when
    try (var stream = Files.walk(Paths.get("src/test/resources/sample-data"))) {
      List<File> files = stream
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".ndjson"))
        .map(Path::toFile)
        .toList();

      // then
      for (File file : files) {
        Pipeline<Log> pipeline = pipelineBuilder.create(
          new LocalFileRepository(),
          LocalFileKey.from(file.toPath()),
          new JSONLogFactory(),
          List.of(new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor())
        );

        Batchable<Log> batcher = new LogBatcher(pipeline, 10_000);

        LogMetadata lastMessageMetadata = LogMetadata.EMPTY;
        while(batcher.hasNextBatch()) {
          List<Log> messages = batcher.nextBatch().get();
          if (messages.isEmpty()) continue;
          lastMessageMetadata = messages.getLast().getMetadata();
          List<String> messageBatch = messages
            .stream()
            .map(Log::get)
            .toList();
          producer.sendBatch(
            new DefaultOffsetRecord(
              lastMessageMetadata.key(),
              lastMessageMetadata.offset()
            ),
            ()-> messageBatch
          );
        }

        if (lastMessageMetadata != LogMetadata.EMPTY) {
          producer.sendBatch(new DefaultOffsetRecord(
            lastMessageMetadata.key(),
            OffsetStatus.COMPLETED.getValue()
          ), Collections::emptyList);
        }

      }
    }
  }
}
