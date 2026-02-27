package info.ankin.projects.jsonschema.jsonschemagen;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.Objects;

@Accessors(chain = true)
@Data
public class GeneratorProperties {
    Path outputDir;
    String packageName;
    String indentation = "  ";
    int maxCommentLineLength = 79;

    public boolean hasPackageName() {
        return Objects.nonNull(getPackageName());
    }
}
