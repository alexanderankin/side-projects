package side.notes.backend;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Accessors(chain = true)
@Component
@ConfigurationProperties(prefix = "notes-backend")
public class NotesBackendProperties {
}
