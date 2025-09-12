package sourceconnector;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.LocalFileOffsetRecord;
import sourceconnector.domain.MessageBatch;
import sourceconnector.domain.factory.FileBaseLogFactory;
import sourceconnector.domain.log.Log;
import sourceconnector.repository.LocalFileRepository;
import sourceconnector.service.batcher.Batchable;
import sourceconnector.service.batcher.LogBatcher;
import sourceconnector.service.pipeline.FileBaseLogPipeline;
import sourceconnector.service.pipeline.Pipeline;
import sourceconnector.service.processor.BaseProcessor;
import sourceconnector.service.processor.impl.EmptyFilterProcessor;
import sourceconnector.service.processor.impl.TrimMapperProcessor;
import sourceconnector.service.producer.BatchProduceService;
import sourceconnector.service.producer.BatchProducer;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@Disabled
class S3SourceConnectorTest {
  private static final Properties props = new Properties();
  static {
    props.putAll(Map.of(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "-1",
        org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, 100,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true,
        ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-s3"
      )
    );
  }

  @DisplayName("Send batch")
  @Test
  void main() {

    File file = Path.of("src/test/resources/sample-data/large-ndjson.ndjson").toFile();

    Pipeline<Log> pipeline = FileBaseLogPipeline.create(
      new LocalFileRepository(),
      file.getPath(),
      new FileBaseLogFactory(),
      new BaseProcessor[]{new TrimMapperProcessor(), new EmptyFilterProcessor()}
    );

    Batchable<Log> batcher = new LogBatcher(pipeline, 1000);
    BatchProducer<String> producer = new BatchProduceService(props, "log", "local-offset");

    // when
    List<Log> messages;
    do {
      MessageBatch<Log> batch = batcher.nextBatch();
      messages = batch.get();
      var lastMessageMetadata = messages.getLast().getMetadata();
      List<String> messageBatch = messages
        .stream()
        .map(Log::get)
        .toList();
      producer.sendBatch(
        new LocalFileOffsetRecord(lastMessageMetadata.key(), lastMessageMetadata.offset()),
        ()-> messageBatch
      );

    } while (messages != Collections.EMPTY_LIST);

  }
}
