package sourceconnector.domain.connect;

import lombok.AccessLevel;
import lombok.Getter;
import offsetmanager.domain.file.FileKey;
import offsetmanager.domain.offset.DefaultOffsetRecord;
import offsetmanager.domain.offset.OffsetStatus;
import sourceconnector.domain.log.Log;
import sourceconnector.domain.log.LogMetadata;
import sourceconnector.domain.pipeline.Pipeline;
import sourceconnector.domain.pipeline.factory.PipelineSupplier;
import sourceconnector.service.batcher.LogBatcher;
import sourceconnector.service.producer.BatchProducer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSourceTask implements Task<FileProcessingResult> {
  private final String id;
  @Getter
  private final int index;
  private final BatchProducer<String> producer;
  private final PipelineSupplier<Log> pipelineSupplier;

  // visible for test
  @Getter(AccessLevel.PACKAGE)
  private final Map<FileKey, Long> fileKeyOffsetMap = new HashMap<>();
  private final FileProcessingResult result = new FileProcessingResult();

  public FileSourceTask(
    int index,
    PipelineSupplier<Log> pipelineSupplier,
    BatchProducer<String> producer
  ) {
    this.id = String.format("Task-%d", index);
    this.index = index;
    this.pipelineSupplier = pipelineSupplier;
    this.producer = producer;
  }

  @Override
  public FileProcessingResult call() throws Exception {
    try {
      for (Map.Entry<FileKey, Long> entry: this.fileKeyOffsetMap.entrySet()) {

        long offset = entry.getValue();
        if (offset == OffsetStatus.COMPLETED.getValue()) {
          result.addSkippedCount();
          continue;
        }
        FileKey fileKey = entry.getKey();
        Pipeline<Log> pipeline = pipelineSupplier.get(fileKey);

        // Progress offset to the next position in the file
        pipeline.toPosition(offset);

        LogBatcher batcher = new LogBatcher(pipeline, 10_000);

        LogMetadata lastMessageMetadata;

        while (batcher.hasNextBatch()) {
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
            ()->messageBatch
          );
        }


        // Complete this file
        producer.sendBatch(
          new DefaultOffsetRecord(
            // This is for handling no Log after filtered
            fileKey,
            OffsetStatus.COMPLETED.getValue()
          ),
          Collections::emptyList
        );

        this.result.addSuccessCount();
      }

      return this.result;
    } finally {
      this.producer.close();
    }
  }

  @Override
  public void assign(Map<FileKey, Long> fileKeys) {
    this.fileKeyOffsetMap.putAll(fileKeys);
    this.result.setTotalCount(fileKeyOffsetMap.size());
  }
}
