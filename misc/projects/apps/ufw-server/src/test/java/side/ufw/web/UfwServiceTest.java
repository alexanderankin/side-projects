package side.ufw.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class UfwServiceTest {
    @SneakyThrows
    @Test
    void parseStatusVerbose() {
        var statusVerboseTxt = new String(Objects.requireNonNull(getClass().getResourceAsStream("/statusVerbose.txt")).readAllBytes());
        var actual = new UfwService().parseVerboseStatus(statusVerboseTxt);
        var expected = JsonMapper.builder().build().readValue(getClass().getResourceAsStream("/statusVerbose.json"), UfwStatus.UfwStatusVerbose.class);
        assertThat(actual, is(expected));
    }
}
