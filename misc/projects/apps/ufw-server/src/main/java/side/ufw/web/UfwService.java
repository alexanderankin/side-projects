package side.ufw.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import side.ufw.web.exec.ExecProperties;
import side.ufw.web.exec.ExecService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static side.ufw.web.exec.ExecUtils.combineExecutableAndArgs;
import static side.ufw.web.exec.ExecUtils.concat;

@Slf4j
@RequiredArgsConstructor
@Service
public class UfwService {
    private ExecService execService;

    @Autowired
    void setExecService(ExecProperties execProperties, List<ExecService> execServices) {
        this.execService = execServices.stream().filter(e -> e.execType() == execProperties.getType()).findAny().orElseThrow();
    }

    public UfwStatus.UfwStatusVerbose statusVerbose() {
        var result = execService.execute(new ExecService.Config(List.of("sudo", "ufw", "status", "verbose")));
        var output = new String(result.out());
        return parseVerboseStatus(output);
    }

    public void addRule(UfwRule ufwRule) {
        var ufwCommand = toCommand(ufwRule);
        execService.execute(new ExecService.Config(combineExecutableAndArgs("sudo", ufwCommand)));
    }

    public void deleteRule(UfwRule ufwRule) {
        var ufwCommand = toCommand(ufwRule);
        var deleteCmd = concat(List.of("sudo", "ufw", "delete"), ufwCommand.subList(2, ufwCommand.size()));
        execService.execute(new ExecService.Config(deleteCmd));
    }

    List<String> toCommand(UfwRule ufwRule) {
        return switch (ufwRule) {
            case UfwRule.UfwRangeRule ufwRangeRule -> {
                var result = new ArrayList<String>(15);
                result.addAll(List.of("allow", "in", "from"));
                var allowInFrom = ufwRangeRule.getAllowInFrom();
                var cidrExpression = allowInFrom.getIpAddress() + "/" + allowInFrom.getMaskSize();
                result.add(cidrExpression);
                result.addAll(List.of("to", "any", "port"));
                var portExpression = ufwRangeRule.getToAnyPort().stream()
                        .map(UfwRule.PortRange::stringify)
                        .collect(Collectors.joining(","));
                result.add(portExpression);
                result.add("proto");
                result.add(ufwRangeRule.getProto().name());
                var comment = ufwRangeRule.getComment();
                if (comment != null) {
                    result.add("comment");
                    result.add(comment);
                }
                yield result;
            }
        };
    }

    UfwStatus.UfwStatusVerbose parseVerboseStatus(String output) {
        var result = new UfwStatus.UfwStatusVerbose();
        var header = true;
        for (var line : output.lines().toList()) {
            if (header) {
                if (Patterns.START_STATUS_LINE.matcher(line).matches()) {
                    header = false;
                    continue;
                }
            }

            if (header) {
                Matcher matcher;
                if ((matcher = Patterns.VERBOSE_STATUS_FIELD_STATUS.matcher(line)).matches()) {
                    result.setStatus(UfwStatus.UfwStatusValue.valueOf(matcher.group(1)));
                } else if ((matcher = Patterns.VERBOSE_STATUS_FIELD_LOGGING.matcher(line)).matches()) {
                    result.setLogging(UfwStatus.UfwLoggingValue.valueOf(matcher.group(1)));
                    result.setLoggingLevel(UfwStatus.UfwLoggingLevelValue.valueOf(matcher.group(2)));
                } else if ((matcher = Patterns.VERBOSE_STATUS_FIELD_POLICIES.matcher(line)).matches()) {
                    result.setDefaultPolicies(new LinkedHashMap<>());
                    var policies = matcher.group(1);
                    for (var policy : policies.split(",\\s{1,10}")) {
                        var policyMatcher = Patterns.VERBOSE_STATUS_POLICY.matcher(policy);
                        if (!policyMatcher.matches())
                            throw new IllegalArgumentException("invalid policy in 'Default:' line");
                        result.getDefaultPolicies()
                                .put(UfwStatus.UfwDefaultType.valueOf(policyMatcher.group(2)),
                                        UfwStatus.UfwDefaultValue.valueOf(policyMatcher.group(1)));
                    }
                } else if ((matcher = Patterns.VERBOSE_STATUS_FIELD_PROFILES.matcher(line)).matches()) {
                    result.setNewProfilePolicy(UfwStatus.UfwNewProfilePolicy.valueOf(matcher.group(1)));
                }
            } else {
                if (line.isBlank() || Pattern.compile("-{3,50}\\s+-{3,50}\\s+-{3,50}").matcher(line).find()) {
                    continue;
                }

                var matcher = Patterns.VERBOSE_STATUS_RULE.matcher(line.trim());
                if (!matcher.find())
                    continue;

                if (result.getRules() == null) {
                    result.setRules(new ArrayList<>());
                }

                result.getRules().add(new UfwStatus.Rule()
                        .setTo(matcher.group(1))
                        .setAction(matcher.group(2))
                        .setFrom(matcher.group(3))
                        .setLog(matcher.group(4))
                        .setComment(matcher.group(5)));
            }
        }

        return result;
    }

    private static class Patterns {
        private static final Pattern START_STATUS_LINE = Pattern.compile("^.{0,50}To.{0,50}Action.{0,50}From.{0,50}$");
        private static final Pattern VERBOSE_STATUS_FIELD_STATUS = Pattern.compile("^.{0,50}Status: (\\S{0,50})$");
        private static final Pattern VERBOSE_STATUS_FIELD_LOGGING = Pattern.compile("^.{0,50}Logging: (\\S{0,50})(?: \\((.{0,50})\\))?$");
        private static final Pattern VERBOSE_STATUS_FIELD_POLICIES = Pattern.compile("^.{0,50}Default: (.{0,250})$");
        private static final Pattern VERBOSE_STATUS_FIELD_PROFILES = Pattern.compile("^.{0,50}New profiles: (\\S{0,50})$");
        private static final Pattern VERBOSE_STATUS_POLICY = Pattern.compile("^(.{0,50}) \\((.{0,50})\\)$");
        private static final Pattern VERBOSE_STATUS_RULE = Pattern.compile(
                "^(.*?)\\s+" +
                        "([A-Z]+\\s+[A-Z]+)\\s+" +
                        "(.*?)" +
                        "(?:\\s+\\((log(?:-[a-z]+)?(?:\\s+limit)?)\\))?" +
                        "(?:\\s+#\\s*(.*))?$"
        );
    }
}
