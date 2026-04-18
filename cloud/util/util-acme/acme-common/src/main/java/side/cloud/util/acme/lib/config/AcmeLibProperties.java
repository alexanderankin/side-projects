package side.cloud.util.acme.lib.config;

import com.fasterxml.jackson.databind.json.JsonMapper;

public class AcmeLibProperties {
    private static final JsonMapper MAPPER = JsonMapper.builder().findAndAddModules().build();
    public static final Property<Boolean> FIPS_MODE = new Property<>("acme-lib.fipsMode", Boolean.class, false);

    public static <T> T readProperty(Property<T> property) {
        var value = System.getProperty(property.name());
        if (value == null)
            return property.defaultValue();
        return MAPPER.convertValue(value, property.type());
    }

    public record Property<T>(String name, Class<T> type, T defaultValue) {
    }
}
