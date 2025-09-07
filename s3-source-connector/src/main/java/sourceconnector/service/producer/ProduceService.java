package sourceconnector.service.producer;

import lombok.extern.slf4j.Slf4j;
import sourceconnector.domain.MessageBatch;
import sourceconnector.domain.OffsetRecord;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Collection;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class ProduceService implements BatchProducer<String> {
  private final String logTopic;
  private final String offsetTopic;
  private final KafkaProducer<String, String> kafkaProducer;

  public ProduceService(Properties properties,
                        String logTopic,
                        String offsetTopic) {
    this.kafkaProducer = new KafkaProducer<>(properties);
    this.logTopic = logTopic;
    this.offsetTopic = offsetTopic;
  }

  @Override
  public void sendBatch(
    OffsetRecord offsetRecord,
    MessageBatch<String> messageBatch
  ) {
    Collection<String> batch = messageBatch.get();

    this.kafkaProducer.initTransactions();
    try {
      this.kafkaProducer.beginTransaction();

      for (String message : batch) {
        this.kafkaProducer.send(new ProducerRecord<>(
          logTopic,
          null,
          message)
        );
      }
      this.kafkaProducer.send(new ProducerRecord<>(
        this.offsetTopic,
        offsetRecord.key(),
        String.valueOf(offsetRecord.offset())
      ));

      this.kafkaProducer.commitTransaction();
    } catch (Exception e) {
      log.error("Abort transaction since {}", e.getMessage());
      this.kafkaProducer.abortTransaction();
    }

  }

}
