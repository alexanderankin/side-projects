package info.ankin.projects.tfe4j.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

class BaseTest {

    protected static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().findAndAddModules().build();
    protected static final String ORG_TOKEN = "";
    protected static final String USER_TOKEN = "";

    @SneakyThrows
    protected String read(String path) {
        return StreamUtils.copyToString(getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
    }
}
