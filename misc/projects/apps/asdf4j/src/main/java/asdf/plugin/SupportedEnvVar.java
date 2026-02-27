package asdf.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SupportedEnvVar {
    ASDF_INSTALL_TYPE("version or ref"),
    ASDF_INSTALL_VERSION("full version number or Git Ref depending on ASDF_INSTALL_TYPE"),
    ASDF_INSTALL_PATH("the path to where the tool should, or has been installed"),
    ASDF_CONCURRENCY("the number of cores to use when compiling the source code. Useful for setting make -j"),
    ASDF_DOWNLOAD_PATH("the path to where the source code or binary was downloaded to by bin/download"),
    ASDF_PLUGIN_PATH("the path the plugin was installed"),
    ASDF_PLUGIN_SOURCE_URL("the source URL of the plugin"),
    ASDF_PLUGIN_PREV_REF("previous git-ref of the plugin repo"),
    ASDF_PLUGIN_POST_REF("updated git-ref of the plugin repo"),
    ASDF_CMD_FILE("resolves to the full path of the file being sourced{"),
    ;
    final String description;
}
