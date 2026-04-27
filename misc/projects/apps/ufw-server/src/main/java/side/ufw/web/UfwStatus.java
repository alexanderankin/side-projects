package side.ufw.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

public interface UfwStatus {
    enum UfwStatusValue { active, inactive, reloading; }
    enum UfwLoggingValue { on, off; }
    enum UfwLoggingLevelValue { off, low, medium, high, full; }
    enum UfwDefaultType { incoming, outgoing, routed; }
    // enum UfwDirection { in, out, routed; } // for rules i guess right
    enum UfwDefaultValue { allow, deny, reject, limit; }
    enum UfwNewProfilePolicy { skip, enable; }

    @Data
    @Accessors(chain = true)
    class UfwStatusVerbose {
        UfwStatusValue status;
        UfwLoggingValue logging;
        UfwLoggingLevelValue loggingLevel;
        Map<UfwDefaultType, UfwDefaultValue> defaultPolicies;
        UfwNewProfilePolicy newProfilePolicy;
        List<Rule> rules;
    }

    @Data
    @Accessors(chain = true)
    class UfwStatusNumbered {
        UfwStatusValue status;
        List<Rule> rules;
    }

    @Data
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Rule {
        Integer number;
        String to;
        String action;
        String from;
        String log;
        String comment;
    }
}
