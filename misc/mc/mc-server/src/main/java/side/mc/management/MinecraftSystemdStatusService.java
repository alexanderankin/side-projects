package side.mc.management;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import side.mc.management.MinecraftManagementApplication.MinecraftWorldDetails.MinecraftWorldServiceState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Service
class MinecraftSystemdStatusService {
    final ProcessRunner processRunner;
    final ObjectMapper objectMapper;
    private final MinecraftManagementApplication.McManagementProperties mcManagementProperties;
    String systemctlShowPropertiesSuffix;

    @PostConstruct
    void init() {
        List<String> systemctlShowProperties = new ArrayList<>(
                // auxiliary terms
                List.of("Id", "ExecMainStartTimestamp", "MainPID", "MemoryCurrent", "MemoryPeak", "CPUUsageNSec", "Result")
        );
        for (var declaredField : MinecraftWorldServiceState.class.getDeclaredFields()) {
            var jsonAlias = declaredField.getAnnotation(JsonAlias.class);
            if (jsonAlias == null)
                continue;

            if (jsonAlias.value().length == 1) {
                systemctlShowProperties.add(jsonAlias.value()[0]);
            }
        }
        systemctlShowPropertiesSuffix = String.join(" ", systemctlShowProperties.stream().map("--property "::concat).toList());
    }

    public MinecraftWorldServiceState state(String name) {
        var services = Arrays.stream(processRunner.run("systemctl --user show " + name + " " + systemctlShowPropertiesSuffix).output().split("\n\n")).filter(StringUtils::hasText).toList();
        if (services.size() != 1)
            return null;

        return serviceToDto(services.getFirst());
    }

    public List<MinecraftWorldServiceState> states(String pattern) {
        return Arrays.stream(processRunner.run("systemctl --user show " + pattern + " " + systemctlShowPropertiesSuffix).output().split("\n\n"))
                .filter(StringUtils::hasText)
                .map(this::serviceToDto)
                .toList();
    }

    public List<MinecraftWorldServiceState> allStates() {
        return states(mcManagementProperties.getSystemdServicePrefix() + "*");
    }

    private MinecraftWorldServiceState serviceToDto(String first) {
        var parsed = Arrays.stream(first.split("\n"))
                .map(e -> Map.entry(e.substring(0, e.indexOf('=')), e.substring(e.indexOf('=') + 1)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (var s : new ArrayList<>(parsed.keySet())) {
            if (parsed.get(s).equals("[not set]")) {
                parsed.remove(s);
            }
        }
        return objectMapper.convertValue(parsed, MinecraftWorldServiceState.class);
    }
}
