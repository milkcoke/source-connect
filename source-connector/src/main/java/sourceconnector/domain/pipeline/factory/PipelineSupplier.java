package sourceconnector.domain.pipeline.factory;

import sourceconnector.domain.log.Log;
import sourceconnector.domain.pipeline.Pipeline;

/**
 * Task 입장에서는 Pipeline 을 어떻게 생성하는 지를 알 필요 없음.
 * Pipeline 은 하나의 작업의 단위고 매번 새롭게 생성됨.
 * Pipeline 을 구성하는 각각의 Processor 는 singleton 객체여선 안됨.
 * 여러 Task 가 같은 Processor node 에 접근할 수 있기 때문임.
 */
@FunctionalInterface
public interface PipelineSupplier {
  Pipeline<Log> get(String filePath);
}
