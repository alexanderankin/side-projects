package side.dist.mergesort;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.file.Path;

@Data
@Accessors(chain = true)
@Component
@ConfigurationProperties(prefix = "app")
public class MergeSortProperties {
    /**
     * where to store uploads while processing
     */
    Path uploadDirectory = Path.of(System.getProperty("java.io.tmpdir"));

    /**
     * database batching size
     */
    int dbBatchSize = 5_000;

    /**
     * for recursive invocation logic
     */
    URI selfBaseUrl = URI.create("http://localhost:8080");
}
