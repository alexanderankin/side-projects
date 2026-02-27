package info.ankin.projects.jsonschema.jsonschemagen;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;

@Accessors(chain = true)
@Data
public class PojoDefinition {
    String className;
    LinkedHashMap<String, FieldDefinition> fields;

    public LinkedHashMap<String, FieldDefinition> getFields() {
        if (fields == null) fields = new LinkedHashMap<>();
        return fields;
    }

    public PojoDefinition append(String name, FieldDefinition fieldDefinition) {
        getFields().put(name, fieldDefinition);
        return this;
    }

    @Accessors(chain = true)
    @Data
    public static class FieldDefinition {
        String comment;
        Class<?> fieldClass;

        public boolean hasComment() {
            return null != getComment();
        }
    }
}
