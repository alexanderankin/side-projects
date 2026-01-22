package asdf.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsdfConfig {
    private static AsdfConfig DEFAULTS = new AsdfConfig()
            .setToolVersionsFilename(".tool-versions")
            .setDataDir(Path.of(System.getProperty("user.home"), ".asdf"))
            // .setConfigFile(Path.of(System.getProperty("user.home"), ".config", "asdf4j.json"))
            ;

    @JsonAlias("ASDF_TOOL_VERSIONS_FILENAME")
    String toolVersionsFilename;

    @JsonAlias("ASDF_DATA_DIR")
    @JsonSerialize(using = SerDe.PathSerializer.class)
    @JsonDeserialize(using = SerDe.PathDeserializer.class)
    Path dataDir;

    /**
     * contains file with {@link AsdfSettings} object
     */
    @JsonSerialize(using = SerDe.PathSerializer.class)
    @JsonDeserialize(using = SerDe.PathDeserializer.class)
    Path configFile;

    /**
     * asdf hardcodes this as of this writing
     */
    URI pluginIndexUrl = URI.create("https://github.com/asdf-vm/asdf-plugins.git");

    transient AsdfSettings asdfSettings;

    /**
     * things stored in the config file
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @Accessors(chain = true)
    public static class AsdfSettings {
        private static final AsdfSettings DEFAULTS = new AsdfSettings()
                .setAlwaysKeepDownload(false)
                //
                ;

        /**
         * default false
         */
        Boolean alwaysKeepDownload;

        // /**
        //  * default true
        //  */
        // Boolean disablePluginShortNameRepository;

        /**
         * null uses all CPUs
         */
        Integer concurrency;
    }

    @Slf4j
    @RequiredArgsConstructor
    public static class AsdfConfigReader {
        private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
        };
        private final JsonMapper jsonMapper;

        public AsdfConfig read() {
            return read(null);
        }

        @SneakyThrows
        public AsdfConfig read(AsdfConfig cliArgumentValues) {
            AsdfConfig combined = cliArgumentValues == null ? fromEnv() : combineWith(cliArgumentValues, fromEnv(), AsdfConfig.class);
            AsdfConfig config = combineWith(combined, DEFAULTS, AsdfConfig.class);

            if (config.configFile != null) {
                var file = config.configFile.toFile();
                if (file.exists()) {
                    AsdfSettings settings = jsonMapper.readValue(file, AsdfSettings.class);
                    config.asdfSettings = combineWith(settings, AsdfSettings.DEFAULTS, AsdfSettings.class);
                }
            }
            return config;
        }

        public AsdfConfig fromEnv() {
            return fromEnv(System.getenv());
        }

        public AsdfConfig fromEnv(Map<String, String> env) {
            AsdfConfig fromEnv = jsonMapper.convertValue(env, AsdfConfig.class);
            log.trace("parsed asdf config from env: {}", fromEnv);
            return fromEnv;
        }

        public <T> T combineWith(T aConfig, T bConfig, Class<T> tClass) {
            var aConfigMap = jsonMapper.convertValue(aConfig, MAP_TYPE_REFERENCE);
            var bConfigMap = jsonMapper.convertValue(bConfig, MAP_TYPE_REFERENCE);
            for (var entry : new ArrayList<>(aConfigMap.entrySet())) {
                if (entry.getValue() == null)
                    aConfigMap.put(entry.getKey(), bConfigMap.get(entry.getKey()));
            }
            T combined = jsonMapper.convertValue(aConfig, tClass);
            log.trace("combined result {} from a: {} and b: {}", combined, aConfig, bConfig);
            return combined;
        }
    }
}
