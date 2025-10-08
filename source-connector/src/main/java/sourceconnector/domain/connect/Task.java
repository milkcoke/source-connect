package sourceconnector.domain.connect;

import java.util.concurrent.Callable;

public interface Task<T> extends Callable<T> {

}
