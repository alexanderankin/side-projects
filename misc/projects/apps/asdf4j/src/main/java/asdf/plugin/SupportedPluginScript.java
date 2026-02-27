package asdf.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static asdf.plugin.SupportedEnvVar.*;

@Getter
@ToString
@RequiredArgsConstructor
public enum SupportedPluginScript {
    LIST_ALL("list-all", Level.REQUIRED, "List all installable versions", List.of(), List.of()),
    DOWNLOAD("download", Level.RECOMMENDED, "Download source code or binary for the specified version", List.of(ASDF_INSTALL_TYPE, ASDF_INSTALL_VERSION, ASDF_INSTALL_PATH, ASDF_DOWNLOAD_PATH), List.of()),
    INSTALL("install", Level.REQUIRED, "Installs the specified version", List.of(ASDF_INSTALL_TYPE, ASDF_INSTALL_VERSION, ASDF_INSTALL_PATH, ASDF_CONCURRENCY), List.of(ASDF_DOWNLOAD_PATH)),
    LATEST_STABLE("latest-stable", Level.RECOMMENDED, "List the latest stable version of the specified tool", List.of(ASDF_INSTALL_TYPE, ASDF_INSTALL_VERSION, ASDF_INSTALL_VERSION), List.of()),
    HELP_OVERVIEW("help.overview", Level.DEFAULT, "Output a general description about the plugin & tool", List.of(ASDF_INSTALL_TYPE, ASDF_INSTALL_VERSION, ASDF_INSTALL_PATH), List.of()),
    HELP_DEPS("help.deps", Level.DEFAULT, "Output a list of dependencies per Operating System", List.of(ASDF_INSTALL_TYPE, ASDF_INSTALL_VERSION, ASDF_INSTALL_PATH), List.of()),
    HELP_CONFIG("help.config", Level.DEFAULT, "Output plugin or tool configuration information", List.of(), List.of()),
    HELP_LINKS("help.links", Level.DEFAULT, "Output a list of links for the plugin or tool", List.of(), List.of()),
    LIST_BIN_PATHS("list-bin-paths", Level.DEFAULT, "List relative paths to directories with binaries to create shims", List.of(), List.of()),
    EXEC_ENV("exec-env", Level.DEFAULT, "Prepare the environment for running the binaries", List.of(), List.of()),
    EXEC_PATH("exec-path", Level.DEFAULT, "Output the executable path for a version of a tool", List.of(), List.of()),
    UNINSTALL("uninstall", Level.DEFAULT, "Uninstall a specific version of a tool", List.of(), List.of()),
    LIST_LEGACY_FILENAMES("list-legacy-filenames", Level.DEFAULT, "Output filenames of legacy version files: .ruby-version", List.of(), List.of()),
    PARSE_LEGACY_FILE("parse-legacy-file", Level.DEFAULT, "Custom parser for legacy version files", List.of(), List.of()),
    POST_PLUGIN_ADD("post-plugin-add", Level.DEFAULT, "Hook to execute after a plugin has been added", List.of(), List.of()),
    POST_PLUGIN_UPDATE("post-plugin-update", Level.DEFAULT, "Hook to execute after a plugin has been updated", List.of(), List.of()),
    PRE_PLUGIN_REMOVE("pre-plugin-remove", Level.DEFAULT, "Hook to execute before a plugin is removed", List.of(), List.of()),
    ;

    private final String binaryName;
    private final Level level;
    private final String description;
    private final List<SupportedEnvVar> required;
    private final List<SupportedEnvVar> optional;

    public enum Level { REQUIRED, RECOMMENDED, DEFAULT }
}
