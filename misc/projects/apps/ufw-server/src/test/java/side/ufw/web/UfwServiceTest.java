package side.ufw.web;

import tools.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import side.ufw.web.UfwRule.PortRange;
import side.ufw.web.UfwRule.Proto;
import side.ufw.web.UfwRule.UfwIpAddressRange.UfwIpV4AddressRange;
import side.ufw.web.UfwRule.UfwRangeRule;

import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class UfwServiceTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @SneakyThrows
    @Test
    void parseStatusVerbose() {
        var statusVerboseTxt = new String(Objects.requireNonNull(getClass().getResourceAsStream("/statusVerbose.txt")).readAllBytes());
        var actual = new UfwService().parseVerboseStatus(statusVerboseTxt);
        var expected = jsonMapper.readValue(getClass().getResourceAsStream("/statusVerbose.json"), UfwStatus.UfwStatusVerbose.class);
        assertThat(actual, is(expected));
    }

    @SneakyThrows
    @Test
    void testCommand() {
        var actual = new UfwService().toCommand(new UfwRangeRule()
                .setAllowInFrom(new UfwIpV4AddressRange()
                        .setIpAddress("192.168.0.0")
                        .setMaskSize(24))
                .setToAnyPort(List.of(PortRange.parse("8080:9000"), PortRange.parse("22")))
                .setProto(Proto.tcp)
                .setComment("example comment"));
        assertThat(actual, is(List.of("allow", "in", "from", "192.168.0.0/24", "to", "any", "port", "8080:9000,22", "proto", "tcp", "comment", "example comment")));
    }
}
