package asdf.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.nio.file.Path;

public interface SerDe {
    class PathSerializer extends JsonSerializer<Path> {
        @Override
        public void serialize(Path value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null)
                gen.writeNull();
            else
                gen.writeString(value.toString());
        }
    }

    class PathDeserializer extends JsonDeserializer<Path> {
        @Override
        public Path deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            var value = p.getValueAsString();
            return value == null ? null : Path.of(value);
        }
    }
}
