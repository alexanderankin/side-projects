package asdf;

import asdf.model.AsdfConfig;
import asdf.model.AsdfConfig.AsdfConfigReader;
import asdf.model.AsdfInfo;
import asdf.model.AsdfVersionProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Getter
public class Asdf {
    final JsonMapper jsonMapper;
    final AsdfConfigReader asdfConfigReader;
    final AsdfConfig asdfConfig;
    final AsdfConfig fromCli;

    public Asdf(AsdfConfig fromCli) {
        this.fromCli = fromCli;
        jsonMapper = JsonMapper.builder().findAndAddModules().build();
        asdfConfigReader = new AsdfConfigReader(jsonMapper);
        asdfConfig = asdfConfigReader.read(fromCli);
    }

    public AsdfInfo info() {
        return new AsdfInfo()
                .setVersion(Arrays.stream(new AsdfVersionProvider().getVersion()).findAny().orElse(null))
                .setEffectiveConfig(asdfConfig)
                .setConfigFromEnv(asdfConfigReader.fromEnv())
                .setConfigFromCli(fromCli)
                .setInstalledPlugins(readPlugins());
    }

    public void addPlugin(String name, String url) {
        validatePluginName(name);
        validatePluginDoesNotExist(name);

        if (url == null)
            throw new UnsupportedOperationException("do not support the index yet");

        gitClone(asdfConfig.getDataDir().resolve("plugins").resolve(name), url);
        // todo hooks
    }

    // todo very important decision to make - do i just require git as a pre-requisite and stay lean? or bundle jgit
    void gitClone(Path folder, String remoteUrl) {
        var ignored = folder.toFile().mkdirs();
        Exec.INSTANCE.execWithInheritedIo(Exec.Config.builder()
                .command("git")
                .command("clone")
                .command(remoteUrl)
                .command(folder.toString())
                .build());
    }

    void validatePluginDoesNotExist(String name) {
        if (asdfConfig.getDataDir().resolve(name).toFile().exists())
            throw new IllegalStateException("plugin " + name + " already exists");
    }

    void validatePluginName(String name) {
        String regex = "^[a-z0-9_-]{1,200}$";
        if (!name.matches(regex)) {
            throw new IllegalArgumentException("invalid name, must match " + regex);
        }
    }

    public List<AsdfInfo.InstalledPlugin> readPlugins() {
        throw new UnsupportedOperationException();
    }
}
