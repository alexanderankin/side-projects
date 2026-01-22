package asdf.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AsdfInfo {
    String version;
    AsdfConfig effectiveConfig;
    AsdfConfig configFromEnv;
    AsdfConfig configFromCli;
    List<InstalledPlugin> installedPlugins;

    @Data
    @Accessors(chain = true)
    public static class InstalledPlugin {
        String name;
        String url;
        String version;
    }
}
