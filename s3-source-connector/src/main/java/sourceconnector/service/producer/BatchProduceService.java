package sourceconnector.service.producer;

import lombok.extern.slf4j.Slf4j;
import offsetmanager.domain.OffsetRecord;
import sourceconnector.domain.batch.MessageBatch;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class BatchProduceService implements BatchProducer<String> {
  private final String logTopic;
  private final String offsetTopic;
  private final KafkaProducer<String, byte[]> kafkaProducer;

  public BatchProduceService(Properties properties,
                             String logTopic,
                             String offsetTopic) {
    this.logTopic = logTopic;
    this.offsetTopic = offsetTopic;
    this.kafkaProducer = new KafkaProducer<>(properties);
    this.kafkaProducer.initTransactions();
  }

  @Override
  public void sendBatch(
    OffsetRecord offsetRecord,
    MessageBatch<String> messageBatch
  ) {
    Collection<String> batch = messageBatch.get();

    try {
      this.kafkaProducer.beginTransaction();

      for (String message : batch) {
        this.kafkaProducer.send(new ProducerRecord<>(
          logTopic,
          null,
          message.getBytes(StandardCharsets.UTF_8))
        );
      }
      this.kafkaProducer.send(new ProducerRecord<>(
        this.offsetTopic,
        offsetRecord.key(),
        ByteBuffer.allocate(Long.BYTES).putLong(offsetRecord.offset()).array()
      ));

      this.kafkaProducer.commitTransaction();
    } catch (Exception e) {
      log.error("Abort transaction since {}", e.getMessage());
      this.kafkaProducer.abortTransaction();
    }

  }

}
