package asdf.model;

import tools.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class AsdfConfigTest {
    JsonMapper jsonMapper = JsonMapper.builder().build();

    @SneakyThrows
    @Test
    void test() {
        var example = jsonMapper.writeValueAsString(new AsdfConfig().setDataDir(Path.of(System.getProperty("user.home"), ".asdf")));
        System.out.println(example);
        System.out.println(jsonMapper.readValue(example, AsdfConfig.class));
    }
}
