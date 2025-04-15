package info.ankin.projects.mc.discordlist.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import info.ankin.projects.mc.discordlist.service.CommandLineExecutor.CommandResult;
import info.ankin.projects.mc.discordlist.service.UfwService.Rule.LogMode;
import info.ankin.projects.mc.discordlist.service.UfwService.Rule.Port;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Validated
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

    List<Rule> listRules() {
        CommandResult commandResult = commandLineExecutor.execUnsafe(ufwFullPath, "status", "verbose");
        System.out.println(commandResult);
        String[] lines = commandResult.getStandardOutput().split("\n");
        int toOffset = -1, actionOffset = -1, fromOffset = -1, miscOffset = -1;
        int i;
        for (i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (Arrays.asList(line.split("\\s+")).equals(List.of("To", "Action", "From"))) {
                int offsetCounter = 0;
                while (line.charAt(offsetCounter) != '-') offsetCounter += 1;
                toOffset = offsetCounter;
                while (line.charAt(offsetCounter) != ' ') offsetCounter += 1;
                while (line.charAt(offsetCounter) != '-') offsetCounter += 1;
                actionOffset = offsetCounter;
                while (line.charAt(offsetCounter) != ' ') offsetCounter += 1;
                while (line.charAt(offsetCounter) != '-') offsetCounter += 1;
                fromOffset = offsetCounter;
                while (line.charAt(offsetCounter) != ' ') offsetCounter += 1;
                while (line.charAt(offsetCounter) != '-') offsetCounter += 1;
                miscOffset = offsetCounter;

                break;
            }
        }

        i += 1; // todo assert we have dashes line
        List<Rule> rules = new ArrayList<>(lines.length); // todo get the right number
        for (; i < lines.length; i++) {
            var line = lines[i];
            String toPart = line.substring(toOffset, actionOffset).strip();
            String actionPart = line.substring(actionOffset, fromOffset).strip();
            String fromPart = line.substring(fromOffset, miscOffset).strip();
            String miscPart = line.substring(miscOffset).strip();

            int poundIndex = miscPart.indexOf('#');
            String commentPart = poundIndex == -1 ? null : miscPart.substring(poundIndex);
            LogMode logMode = miscPart.startsWith("(")
                    ? (
                    miscPart.contains("(log-all)")
                            ? LogMode.log_all
                            : LogMode.log)
                    : LogMode.quiet;

            if (commentPart == null || !commentPart.contains("managed by UfwService"))
                // safety
                continue;

            rules.add(new Rule()
                    .setPort(Port.parse(toPart))
                    .setAction(Rule.Action.parse(actionPart))
                    .setFrom(Rule.IpRange.parseUfw(fromPart))
                    .setLog(logMode)
                    .setComment(commentPart));
        }
        return rules;
    }

    public void addRule(@Valid UfwService.Rule rule) {
        String.format("ufw %s from %s/%s to any port %s proto %s comment %s",
                rule.getAction().stringify().toLowerCase(),
                rule.getFrom().getAddress(),
                rule.getFrom().getMaskLength(),
                rule.getPort().getPort(),
                rule.getPort().getProto());
    }

    void denyRule(Rule rule) {
    }

    public static class UfwException extends RuntimeException {
        UfwException(String message) {
            super(message);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Rule {
        @NotNull
        @Valid
        Port port;
        @NotNull
        @Valid
        Action action;
        @NotNull
        @Valid
        IpRange from;
        LogMode log;
        String comment;

        public enum LogMode {
            quiet, log, log_all,
        }

        @Data
        @Accessors(chain = true)
        public static class Action {
            Mode mode;
            Direction direction;

            @JsonCreator
            public static Action parse(String allowIn) {
                String[] split = allowIn.split(" ");
                return new Action().setMode(Mode.valueOf(split[0])).setDirection(Direction.valueOf(split[1]));
            }

            @JsonValue
            public String stringify() {
                return mode + " " + direction;
            }

            public enum Mode {
                ALLOW, DENY
            }

            public enum Direction {
                IN, OUT
            }
        }

        @Data
        @Accessors(chain = true)
        public static class Port {
            int port;
            String proto;
            String interfaceName;

            public static Port parse(String toPart) {
                Port result = new Port();
                if (toPart.contains(" on ")) {
                    String[] split = toPart.split(" on ");
                    toPart = split[0];
                    result.setInterfaceName(split[1]);
                } else {
                    result.setInterfaceName(null);
                }

                if (toPart.contains("/")) {
                    String[] split = toPart.split("/");
                    return result
                            .setPort(Integer.parseInt(split[0]))
                            .setProto(split[1]);
                }

                return result.setPort(Integer.parseInt(toPart));
            }
        }

        @Data
        @Accessors(chain = true)
        public static class IpRange {
            String address;
            int maskLength;

            public static IpRange parseUfw(String fromPart) {
                if (fromPart.contains("/")) {
                    String[] split = fromPart.split("/");
                    return new IpRange()
                            .setAddress(split[0])
                            .setMaskLength(Integer.parseInt(split[1]))
                            ;
                }
                boolean ipv6 = fromPart.contains(":");
                int length = ipv6 ? 128 : 32;
                return new IpRange().setAddress(fromPart).setMaskLength(length);
            }
        }
    }
}
