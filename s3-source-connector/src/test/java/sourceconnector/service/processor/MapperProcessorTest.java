package sourceconnector.service.processor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapperProcessorTest {

  @Test
  void process() {
    BaseProcessor<String, String> processor = new MapperProcessor<>(String::length)
      .setNext(new MapperProcessor<>(num-> num.toString()));
  }
}
