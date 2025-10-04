package sourceconnector.repository.file;

import java.io.IOException;
import java.util.List;

public interface FileLister {
    List<String> listFiles(String path) throws IOException;
}
