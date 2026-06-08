package info.ankin.projects.jsonschema.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import tools.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClassAsStringTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Test
    void test_jsonCreator() {
        WithCreator s = objectMapper.readValue("\"abc\"", WithCreator.class);
        assertThat(s, is(notNullValue()));
        assertThat(s.getS(), is("abc"));
    }

    @SneakyThrows
    @Test
    void test_noJsonCreator() {
        assertThrows(JsonProcessingException.class,
                () -> objectMapper.readValue("\"abc\"", WithOutCreator.class));

    }

    @Data
    public static class WithCreator {
        String s;

        @JsonCreator
        public WithCreator(String s) {
            this.s = s;
        }
    }

    @Data
    public static class WithOutCreator {
        String s;
    }
}
