package sourceconnector.service.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NdjsonBatchReader implements BatchReader<String> {
  private final BufferedReader reader;
  private final int batchSize;

  public NdjsonBatchReader(InputStream inputStream, int batchSize) {
    this.reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    this.batchSize = batchSize;
  }

  @Override
  public List<String> nextBatch() throws IOException {
    List<String> batch = new ArrayList<>(batchSize);
    String line;
    while (batch.size() < batchSize && (line = reader.readLine()) != null) {
      batch.add(line);
    }

    return batch;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
