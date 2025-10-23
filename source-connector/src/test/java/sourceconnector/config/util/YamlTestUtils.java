package sourceconnector.config.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.Map;

public class YamlTestUtils {
  private YamlTestUtils() {}
  @SuppressWarnings("unchecked")
  @NotNull
  public static Map<String, Object> getStringObjectMap(String yamlStr) throws IOException {

    var loader = new YamlPropertySourceLoader();
    var resource = new ByteArrayResource(yamlStr.getBytes());
    var propertySource = loader.load("test", resource).getFirst();

    // Convert PropertySource to MapConfigurationPropertySource for Binder
    return (Map<String, Object>) propertySource.getSource();
  }
}
