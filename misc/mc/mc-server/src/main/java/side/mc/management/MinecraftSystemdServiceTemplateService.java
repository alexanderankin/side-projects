package side.mc.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import side.mc.management.MinecraftManagementApplication.McManagementProperties;
import side.mc.management.MinecraftManagementApplication.MinecraftVersion;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@SuppressWarnings("CommentedOutCode")
@RequiredArgsConstructor
@Service
class MinecraftSystemdServiceTemplateService {
    private final ResourceLoader resourceLoader;
    private final McManagementProperties props;
    private final ObjectMapper objectMapper;
    private String serviceTemplate;

    @SneakyThrows
    @PostConstruct
    void readTemplateFromClasspath() {
        // Load raw template
        serviceTemplate = resourceLoader
                .getResource("classpath:/side/mc/management/service-template.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        // // TEST round-trip
        // MinecraftManagementApplication.MinecraftVersion input = new MinecraftManagementApplication.MinecraftVersion()
        //         .setName("myworld")
        //         .setDisplayName("My World")
        //         .setServerJarDownloadUrl(URI.create("https://example.com/server.jar"))
        //         .setMaxG(4)
        //         .setMinG(1);
        //
        // String rendered = render(input);
        // System.out.println("---- Rendered ----\n" + rendered);
        //
        // MinecraftManagementApplication.MinecraftVersion parsed = readVersion(rendered);
        // System.out.println("---- Parsed ----\n" + parsed);
    }

    @SneakyThrows
    String render(MinecraftVersion version) {
        return "# " + objectMapper.writeValueAsString(version) + "\n" + serviceTemplate
                .replace("{{DISPLAY_NAME}}", Objects.requireNonNullElse(version.getDisplayName(), version.getName()))
                .replace("{{MAX_G}}", version.getMaxG().toString())
                .replace("{{MIN_G}}", version.getMinG().toString())
                .replace("{{JAR_PATH}}", props.getJarsDir().resolve(version.getName()).toString())
                .replace("{{SERVER_JAR_DOWNLOAD_URL}}", version.getServerJarDownloadUrl().toString())
                .replace("{{WORLDS_DIR}}", props.getWorldsDir().toString());
    }

    @SneakyThrows
    MinecraftVersion readVersion(String renderedContent) {
        var firstLine = renderedContent.lines().findFirst().orElse(null);
        if (firstLine == null || !firstLine.startsWith("# {")) {
            return null;
        }

        return objectMapper.readValue((firstLine.split("# ")[1]), MinecraftVersion.class);
    }
}
