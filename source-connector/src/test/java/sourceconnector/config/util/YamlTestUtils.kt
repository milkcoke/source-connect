package sourceconnector.config.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlTestUtils {
  private YamlTestUtils() {}

  /**
   * Used for mimicking SpringBoot externalized configuration mapping behavior <br>
   * @throws IOException
   * @see <a href=https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding>SpringBoot External Configuration</a>
   */
  @SuppressWarnings("unchecked")
  @NotNull
  public static Map<String, Object> getStringObjectMap(String yamlStr) throws IOException {
    var loader = new YamlPropertySourceLoader();
    var resource = new ByteArrayResource(yamlStr.getBytes());
    var propertySource = loader.load("test", resource).getFirst();

    var source =  (Map<String, Object>) propertySource.getSource();
    return unwrapOriginTrackedValues(source);
  }

  private static Map<String, Object> unwrapOriginTrackedValues(Map<String, Object> source) {
    return source.entrySet().stream()
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> (e.getValue() instanceof OriginTrackedValue v) ? v.getValue() : e.getValue()
      ));
  }
}
