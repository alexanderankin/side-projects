package side.oci.helpers;

import lombok.Data;
import lombok.experimental.Accessors;
import side.oci.helpers.model.Config;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Accessors(chain = true)
public class OciHelpersConfig {
    Path configPath = Paths.get(System.getProperty("user.home"), ".oci", "config");
    String profile = "DEFAULT";
    Config config;
}
