package sourceconnector.service.producer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.DisplayName;
import sourceconnector.domain.MessageBatch;
import sourceconnector.domain.DefaultMessageBatchLogs;
import sourceconnector.domain.OffsetRecord;
import sourceconnector.domain.S3OffsetRecord;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;


class ProduceServiceTest {
  private static final Properties props = new Properties();
  static {
    props.putAll(Map.of(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "-1",
        org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, 100,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
//        ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-s3"
      )
    );
  }
  @Test
  void sendTest() {
    // given
    ProduceService produceService = new ProduceService(
      props,
      "log-topic",
      "s3-offset-topic"
    );
    MessageBatch<String> messageBatch = new DefaultMessageBatchLogs(List.of(
      "log1",
      "log2",
      "log3"
    ));
    OffsetRecord offsetRecord = new S3OffsetRecord("s3://test/2025/04/11/test.json", 3L);
    // when
    produceService.sendBatch(offsetRecord, messageBatch);


  }

  @DisplayName("Message 1000개 미리 쌓아놓고 100개 사라지면?")
  @Test
  void AthousandMessagesTest() {
    // given
    try (KafkaProducer<String, byte[]> producer = new KafkaProducer<>(props)) {
//      producer.initTransactions();
//      producer.beginTransaction();
      for (int i = 1001; i <= 1_001; i++) {
        producer.send(new ProducerRecord<>("offset-test-topic", String.valueOf(i), ByteBuffer
          .allocate(Integer.BYTES)
          .putInt(i)
          .array()));
      }
      producer.flush();
//      producer.commitTransaction();
    } catch (Exception e) {
      System.out.println("실패 ㅋ");
    }
  }

  @DisplayName("Consumer 미리 300개 쌓아놓고 첫 오프셋은 301인데 1000번까지 쌓인 상황에서 0부터 poll()")
  @Test
  void pollThousandTest() {
    // given
    Properties props = new Properties();
    props.putAll(Map.of(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class,
      ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 2000,
      ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 52428800,
      ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500
    ));
    try(Consumer<byte[], Integer> consumer = new KafkaConsumer<>(props)) {

      TopicPartition topicPartition = new TopicPartition("offset-test-topic", 0);
      consumer.assign(List.of(topicPartition));
      // when
      consumer.seek(topicPartition,100);

      // then
      var consumerRecords = consumer.poll(Duration.ofSeconds(60L));
      List<ConsumerRecord<byte[], Integer>> records = consumerRecords.records(topicPartition);
      System.out.println("Size : " + records.size());
      System.out.println("Max : " + records.stream().max(Comparator.comparingInt(ConsumerRecord::value)));
      System.out.println("Min : " + records.stream().min(Comparator.comparingInt(ConsumerRecord::value)));

      var secondConsumerRecords = consumer.poll(Duration.ofSeconds(120L));
      var secondRecords = secondConsumerRecords.records(topicPartition);
      System.out.println("Size : " + secondRecords.size());
      System.out.println("Max : " + secondRecords.stream().max(Comparator.comparingInt(ConsumerRecord::value)));
      System.out.println("Min : " +  secondRecords.stream().min(Comparator.comparingInt(ConsumerRecord::value)));

    }
  }
}
