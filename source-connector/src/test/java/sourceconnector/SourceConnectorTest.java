package sourceconnector;

import offsetmanager.domain.file.LocalFileKey;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.LogMetadata;
import sourceconnector.domain.log.factory.JSONLogFactory;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.pipeline.factory.FileBaseLogPipelineBuilder;
import sourceconnector.domain.pipeline.factory.PipelineBuilder;
import sourceconnector.domain.processor.impl.EmptyFilterProcessor;
import sourceconnector.domain.processor.impl.TrimMapperProcessor;
import sourceconnector.repository.file.LocalFileRepository;
import sourceconnector.service.batcher.Batchable;
import sourceconnector.service.batcher.LogBatcher;
import sourceconnector.service.producer.BatchProduceService;
import sourceconnector.service.producer.BatchProducer;
import sourceconnector.support.KafkaTestSupport;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

class SourceConnectorTest extends KafkaTestSupport {

  private final String logTopic = "temp-log-test";
  private final String offsetTopic = "temp-offset-test";

  @DisplayName("Send batch")
  @Test
  void mainTest() throws Exception {

    PipelineBuilder<Log> pipelineBuilder = new FileBaseLogPipelineBuilder();
    Path localPath = Path.of("src/test/resources/sample-data/large.ndjson");

    Pipeline<Log> pipeline = pipelineBuilder.create(
      new LocalFileRepository(),
      LocalFileKey.from(localPath),
      new JSONLogFactory(),
      List.of(new TrimMapperProcessor(new JSONLogFactory()), new EmptyFilterProcessor())
    );
    Batchable<Log> batcher = new LogBatcher(pipeline, 10_000);

    try (BatchProducer<String> producer = new BatchProduceService(producerProperties, logTopic, offsetTopic)){
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


  }

  @DisplayName("Should handle multiple files")
  @Test
  void handleDirectory() throws Exception {
    // given

    // when
    try (
      BatchProducer<String> producer = new BatchProduceService(producerProperties, logTopic, offsetTopic);
      var stream = Files.walk(Paths.get("src/test/resources/sample-data"))
    ) {
      List<File> files = stream
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".ndjson"))
        .map(Path::toFile)
        .toList();

      // then
      PipelineBuilder<Log> pipelineBuilder = new FileBaseLogPipelineBuilder();
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
  void NothingToDoAfterProcessingAllFiles() throws Exception {
    // given
    try (
      BatchProducer<String> producer = new BatchProduceService(producerProperties, logTopic, offsetTopic);
      // when
      var stream = Files.walk(Paths.get("src/test/resources/sample-data"))
    ) {
      List<File> files = stream
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".ndjson"))
        .map(Path::toFile)
        .toList();

      // then
      PipelineBuilder<Log> pipelineBuilder = new FileBaseLogPipelineBuilder();
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
