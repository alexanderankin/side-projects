package info.ankin.projects.mc.discordlist.service;

import info.ankin.projects.mc.discordlist.service.CommandLineExecutor.CommandResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UfwService {
    private final CommandLineExecutor commandLineExecutor;
    private String ufwFullPath;

    @PostConstruct
    void init() {
        ufwFullPath = commandLineExecutor.fullPath("ufw");
    }

    boolean enabled() {
        CommandResult result = commandLineExecutor.execUnsafe(ufwFullPath, "status");
        return switch (result.getStandardOutput()) {
            case "Status: inactive\n" -> false;
            case "Status: active\n" -> true;
            default -> throw new UfwException("could not parse result: " + result);
        };
    }

    void enable(boolean enable) {
        CommandResult commandResult = commandLineExecutor.execUnsafe(ufwFullPath, enable ? "enable" : "disable");
        System.out.println(commandResult);
    }

    public static class UfwException extends RuntimeException {
        UfwException(String message) {
            super(message);
        }
    }
}
