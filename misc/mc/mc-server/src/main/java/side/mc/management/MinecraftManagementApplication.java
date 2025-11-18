package side.mc.management;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import side.mc.management.MinecraftManagementApplication.MinecraftWorldDetails.MinecraftWorldServiceState.ActiveState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SuppressWarnings("CommentedOutCode")
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
class MinecraftManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(MinecraftManagementApplication.class, args);
    }

    static Properties toProperties(Map<Object, Object> map) {
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    @Slf4j
    @RequiredArgsConstructor
    @RestController
    @RequestMapping(path = "/api/worlds")
    @Validated
    static class WorldController {
        final McManagementProperties props;
        final VersionController versionController;
        private final ProcessRunner processRunner;
        private final MinecraftSystemdStatusService minecraftSystemdStatusService;

        @SneakyThrows
        @PostMapping
        MinecraftWorld createWorld(@Valid @RequestBody MinecraftWorld world) {
            if (anyWorldsUsingName(world.getName()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "another server already using this name");
            if (anyWorldsOnPort(world.getPort()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "another server already using this port");
            if (versionController.get(world.getVersionName()) == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no such version known");

            Path worldDir = props.getWorldsDir().resolve(world.getName());

            // noinspection ResultOfMethodCallIgnored
            worldDir.toFile().mkdirs();
            Files.writeString(worldDir.resolve("eula.txt"), "eula=true");
            writeProperties(world.toServerProperties(), worldDir.resolve("server.properties").toFile());
            writeProperties(toProperties(Map.of("version", world.getVersionName())), worldDir.resolve("index.properties").toFile());

            processRunner.run("systemctl --user start " + serviceName(world));
            return world;
        }

        @SneakyThrows
        @GetMapping
        Page<MinecraftWorld> listWorlds(Pageable pageable) {
            try (var totalCount = Files.list(props.getWorldsDir())) {
                var total = totalCount.count();
                try (var list = Files.list(props.getWorldsDir())) {
                    var result = list.skip(pageable.getOffset())
                            .limit(pageable.getPageSize())
                            .map(this::getMinecraftWorld)
                            .toList();
                    return new PageImpl<>(result, pageable, total);
                }
            }
        }

        private MinecraftWorld getMinecraftWorld(Path worldPath) {
            var indexProperties = readProperties(worldPath.resolve("index.properties").toFile());
            var serverProperties = readProperties(worldPath.resolve("server.properties").toFile());
            return new MinecraftWorld()
                    .setName(worldPath.getFileName().toString())
                    .setVersionName(indexProperties.getProperty("version"))
                    .setMotd(serverProperties.getProperty("motd"))
                    .setSeed(serverProperties.getProperty("level-seed"))
                    .setPort(Integer.valueOf(serverProperties.getProperty("server-port")));
        }

        @GetMapping(path = "/{id}")
        MinecraftWorld get(@PathVariable String id) {
            return getMinecraftWorld(props.getWorldsDir().resolve(id));
        }

        @PutMapping(path = "/{id}/status/{status}")
        ActiveState updateStatus(@PathVariable String id, @PathVariable ActiveState status) {
            var world = get(id);
            switch (status) {
                case active -> start(world);
                case inactive -> stop(world);
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid status - support active/inactive");
            }

            return minecraftSystemdStatusService.state(serviceName(world)).active;
        }

        void start(MinecraftWorld world) {
            if (minecraftSystemdStatusService.state(serviceName(world)).active == ActiveState.active) {
                return;
            }

            processRunner.run("systemctl --user start " + serviceName(world));
        }

        void stop(MinecraftWorld world) {
            if (minecraftSystemdStatusService.state(serviceName(world)).active != ActiveState.active) {
                return;
            }

            processRunner.run("systemctl --user stop " + serviceName(world));
        }

        String serviceName(MinecraftWorld world) {
            return props.getSystemdServicePrefix() + world.getVersionName() + "@" + world.getName();
        }

        @GetMapping(path = "/{id}/details")
        MinecraftWorldDetails getDetails(@PathVariable String id) {
            var world = getMinecraftWorld(props.getWorldsDir().resolve(id));

            return new MinecraftWorldDetails()
                    .setWorld(world)
                    .setServiceState(minecraftSystemdStatusService.state(serviceName(world)));
        }

        @SneakyThrows
        boolean anyWorldsUsingName(String name) {
            try (var files = Files.list(props.getWorldsDir())) {
                return files.anyMatch(p -> p.getFileName().toString().equals(name));
            }
        }

        @SneakyThrows
        boolean anyWorldsOnPort(int port) {
            try (var files = Files.list(props.getWorldsDir())) {
                return files.map(d -> d.resolve("server.properties")).map(Path::toFile).filter(File::exists)
                        .map(this::readProperties).map(p -> p.getProperty("server-port")).filter(Objects::nonNull).map(Integer::parseInt)
                        .anyMatch(Predicate.isEqual(port));
            }
        }

        @SneakyThrows
        private Properties readProperties(File file) {
            Properties properties = new Properties();
            try (FileReader reader = new FileReader(file)) {
                properties.load(reader);
            }
            return properties;
        }

        @SneakyThrows
        private void writeProperties(Properties properties, File file) {
            try (FileOutputStream out = new FileOutputStream(file)) {
                properties.store(out, "Minecraft server properties - managed");
            }
        }
    }

    @RequiredArgsConstructor
    @RestController
    @RequestMapping(path = "/api/versions")
    @Validated
    static class VersionController {
        final McManagementProperties props;
        final ProcessRunner processRunner;
        final MinecraftSystemdServiceTemplateService templateService;
        final RestTemplateBuilder builder;

        @SneakyThrows
        @PostMapping
        MinecraftVersion create(@Valid @RequestBody MinecraftVersion version) {
            if (get(version.getName()) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Version already exists");
            }

            var jarPath = props.getJarsDir().resolve(version.getName());
            if (!Files.exists(jarPath)) {
                builder.build().execute(version.getServerJarDownloadUrl(), HttpMethod.GET, null, response -> {
                    Files.copy(response.getBody(), jarPath);
                    return null;
                });
            }

            Files.writeString(toServicePath(version.getName()), templateService.render(version), StandardCharsets.UTF_8);
            processRunner.run("systemctl --user daemon-reload");
            return version;
        }

        @SneakyThrows
        @GetMapping
        Page<MinecraftVersion> get(Pageable pageable) {
            try (var counting = Files.list(props.getSystemdUserServiceDir())) {
                var count = counting.count();
                try (var files = Files.list(props.getSystemdUserServiceDir())) {
                    return new PageImpl<>(files
                            .filter(e -> e.getFileName().toString().startsWith(props.getSystemdServicePrefix()))
                            .skip(pageable.getOffset())
                            .limit(pageable.getPageSize())
                            .map(this::readPath)
                            .toList(), pageable, count);
                }
            }
        }

        @GetMapping(path = "/{name}")
        MinecraftVersion get(@PathVariable String name) {
            var servicePath = toServicePath(name);
            if (!servicePath.toFile().exists()) {
                return null;
            }
            return readPath(servicePath);
        }

        @GetMapping(path = "/{name}/details")
        MinecraftVersionDetails getInfo(@PathVariable String name) {
            var mv = Optional.ofNullable(get(name)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            return new MinecraftVersionDetails()
                    .setMinecraftVersion(mv)
                    .setFileName(toServicePath(name).getFileName().toString())
                    .setJarExists(toServicePath(name).toFile().exists());
        }

        @SneakyThrows
        private MinecraftVersion readPath(Path servicePath) {
            return templateService.readVersion(Files.readString(servicePath));
        }

        Path toServicePath(String name) {
            return props.getSystemdUserServiceDir().resolve(props.getSystemdServicePrefix() + name + "@.service");
        }
    }

    @Data
    @Accessors(chain = true)
    static class MinecraftVersion {
        @NotBlank
        @Pattern(regexp = "[\\w-]{1,100}")
        String name;
        @Pattern(regexp = "[\\w- ]{1,100}")
        String displayName;
        URI serverJarDownloadUrl;
        Integer maxG;
        Integer minG;

        @AssertTrue
        boolean isServerJarDownloadHttps() {
            return serverJarDownloadUrl == null || "https".equals(serverJarDownloadUrl.getScheme());
        }
    }

    @Data
    @Accessors(chain = true)
    static class MinecraftVersionDetails {
        MinecraftVersion minecraftVersion;
        String fileName;
        boolean jarExists;
        // Integer serversTotal;
        // Integer serversOn;
        // Integer serversOff;
    }

    @Data
    @Accessors(chain = true)
    static class MinecraftWorld {
        @Pattern(regexp = "[\\w-_]{1,50}")
        @NotBlank
        String name;
        @NotBlank
        String versionName;
        @Pattern(regexp = "[\\w-_'\" ]{1,50}")
        String motd;
        @Pattern(regexp = "-?\\d{1,50}")
        String seed;
        @NotNull
        Integer port;

        public Properties toServerProperties() {
            var properties = new Properties();

            properties.put("accepts-transfers", "false");
            properties.put("allow-flight", "false");
            properties.put("allow-nether", "true");
            properties.put("broadcast-console-to-ops", "true");
            properties.put("broadcast-rcon-to-ops", "true");
            properties.put("bug-report-link", "");
            properties.put("difficulty", "normal");
            properties.put("enable-code-of-conduct", "false");
            properties.put("enable-command-block", "false");
            properties.put("enable-jmx-monitoring", "false");
            properties.put("enable-query", "false");
            properties.put("enable-rcon", "false");
            properties.put("enable-status", "true");
            properties.put("enforce-secure-profile", "true");
            properties.put("enforce-whitelist", "false");
            properties.put("entity-broadcast-range-percentage", "100");
            properties.put("force-gamemode", "false");
            properties.put("function-permission-level", "2");
            properties.put("gamemode", "survival");
            properties.put("generate-structures", "true");
            properties.put("generator-settings", "{}");
            properties.put("hardcore", "false");
            properties.put("hide-online-players", "false");
            properties.put("initial-disabled-packs", "");
            properties.put("initial-enabled-packs", "vanilla");
            properties.put("level-name", "world");
            properties.put("level-seed", Objects.requireNonNullElse(seed, ""));
            properties.put("level-type", "minecraft:normal");
            properties.put("log-ips", "true");
            properties.put("management-server-enabled", "false");
            properties.put("management-server-host", "localhost");
            properties.put("management-server-port", "0");
            properties.put("management-server-secret", "");
            properties.put("management-server-tls-enabled", "true");
            properties.put("management-server-tls-keystore", "");
            properties.put("management-server-tls-keystore-password", "");
            properties.put("max-chained-neighbor-updates", "1000000");
            properties.put("max-players", "20");
            properties.put("max-tick-time", "60000");
            properties.put("max-world-size", "29999984");
            properties.put("motd", Optional.ofNullable(motd).orElse(name));
            properties.put("network-compression-threshold", "256");
            properties.put("online-mode", "true");
            properties.put("op-permission-level", "4");
            properties.put("pause-when-empty-seconds", "60");
            properties.put("player-idle-timeout", "0");
            properties.put("prevent-proxy-connections", "false");
            properties.put("pvp", "true");
            properties.put("query.port", "25565");
            properties.put("rate-limit", "0");
            properties.put("rcon.password", "");
            properties.put("rcon.port", "25575");
            properties.put("region-file-compression", "deflate");
            properties.put("require-resource-pack", "false");
            properties.put("resource-pack", "");
            properties.put("resource-pack-id", "");
            properties.put("resource-pack-prompt", "");
            properties.put("resource-pack-sha1", "");
            properties.put("server-ip", "");
            properties.put("server-port", String.valueOf(port));
            properties.put("simulation-distance", "10");
            properties.put("spawn-monsters", "true");
            properties.put("spawn-protection", "16");
            properties.put("status-heartbeat-interval", "0");
            properties.put("sync-chunk-writes", "true");
            properties.put("text-filtering-config", "");
            properties.put("text-filtering-version", "0");
            properties.put("use-native-transport", "true");
            properties.put("view-distance", "10");
            properties.put("white-list", "false");

            return properties;
        }
    }

    @Data
    @Accessors(chain = true)
    static class MinecraftWorldDetails {
        MinecraftWorld world;
        MinecraftWorldServiceState serviceState;

        @Data
        @Accessors(chain = true)
        static class MinecraftWorldServiceState {
            @JsonAlias("ExecMainStartTimestamp")
            String startTimeStamp;
            @JsonAlias("ExecMainExitTimestamp")
            String exitTimeStamp;
            @JsonAlias("MainPID")
            Integer mainPid;
            @JsonAlias("CPUUsageNSec")
            Long cpuUsageNanos;
            @JsonAlias("ActiveState")
            ActiveState active;
            String test;

            @RequiredArgsConstructor
            @Getter
            @ToString
            enum ActiveState {
                active("Started, bound, plugged in, …, depending on the unit type."),
                inactive("Stopped, unbound, unplugged, …, depending on the unit type."),
                failed("Similar to inactive, but the unit failed in some way (process returned error code on exit, crashed, an operation timed out, or after too many restarts)."),
                activating("Changing from inactive to active."),
                deactivating("Changing from active to inactive."),
                maintenance("Unit is inactive and a maintenance operation is in progress."),
                reloading("Unit is active and it is reloading its configuration."),
                refreshing("Unit is active and a new mount is being activated in its namespace."),
                ;
                final String description;
            }
        }
    }


    @Slf4j
    @Data
    @Accessors(chain = true)
    @Component
    @ConfigurationProperties(prefix = "mc.mgmt")
    @Validated
    static class McManagementProperties implements InitializingBean {
        @NotNull
        Path systemdUserServiceDir = Path.of(System.getProperty("user.home"), ".config", "systemd", "user");
        @NotNull
        Path worldsDir = Path.of(System.getProperty("user.home"), "mc-servers");
        @NotNull
        Path jarsDir = Path.of(System.getProperty("user.home"), "mc-server-versions");
        @NotBlank
        @Length(min = 5)
        @Pattern(regexp = ".*-$")
        String systemdServicePrefix = "minecraft-management-managed-";

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void afterPropertiesSet() {
            systemdUserServiceDir.toFile().mkdirs();
            worldsDir.toFile().mkdirs();
            jarsDir.toFile().mkdirs();
        }
    }
}
