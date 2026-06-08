package asdf.model;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

import java.nio.file.Path;

public interface SerDe {
    class PathSerializer extends ValueSerializer<Path> {
        @Override
        public void serialize(Path value, JsonGenerator gen, SerializationContext serializers) {
            if (value == null)
                gen.writeNull();
            else
                gen.writeString(value.toString());
        }
    }

    class PathDeserializer extends ValueDeserializer<Path> {
        @Override
        public Path deserialize(JsonParser p, DeserializationContext ctxt) {
            var value = p.getValueAsString();
            return value == null ? null : Path.of(value);
        }
    }
}
