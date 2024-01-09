package tf.ideas.idea1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Configuration {
    private List<Variable> variables;
    private List<Resource> resources;
    private List<String> outputs;

    public Configuration add(Variable variable) {
        variables().add(variable);
        return this;
    }

    public List<Variable> variables() {
        if (variables == null) variables = new ArrayList<>();
        return variables;
    }

    public Configuration add(Resource resource) {
        resources().add(resource);
        return this;
    }

    public List<Resource> resources() {
        if (resources == null) resources = new ArrayList<>();
        return resources;
    }

    public Configuration add(String output) {
        outputs().add(output);
        return this;
    }

    public List<String> outputs() {
        if (outputs == null) outputs = new ArrayList<>();
        return outputs;
    }

    @Data
    @Accessors(chain = true)
    public static class Resource {
        Mode mode;
        String resourceType;
        String name;
        List<Property> properties;

        public Resource() {
        }

        public Resource(String name) {
            this.name = name;
        }

        public Resource add(Property property) {
            properties().add(property);
            return this;
        }

        public List<Property> properties() {
            if (properties == null) properties = new ArrayList<>();
            return properties;
        }

        public enum Mode {
            DATA, MANAGED
        }
    }

    @Data
    @Accessors(chain = true)
    public static sealed abstract class Property {
        String name;

        public static Property stringProperty(String name, String s) {
            return new ScalarProperty().setValue(TextNode.valueOf(s)).setName(name);
        }

        public static Property numberProperty(String name, Integer n) {
            return new ScalarProperty().setValue(IntNode.valueOf(n)).setName(name);
        }

        public static Property numberProperty(String name, Double n) {
            return new ScalarProperty().setValue(DoubleNode.valueOf(n)).setName(name);
        }

        public static Property boolProperty(String name, Boolean b) {
            return new ScalarProperty().setValue(BooleanNode.valueOf(b)).setName(name);
        }

        public static ListProperty listProperty(String name) {
            return new ListProperty(name);
        }

        public static ObjectProperty objectProperty(String name) {
            return new ObjectProperty(name);
        }

        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        @Data
        @Accessors(chain = true)
        public static final class ScalarProperty extends Property {
            JsonNode value;
        }

        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        @Data
        @Accessors(chain = true)
        public static final class ListProperty extends Property {
            List<ScalarProperty> values;

            public ListProperty(String name) {
                this.name = name;
            }

            public ListProperty add(ScalarProperty scalarProperty) {
                values().add(scalarProperty);
                return this;
            }

            public List<ScalarProperty> values() {
                if (values == null) values = new ArrayList<>();
                return values;
            }
        }

        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        @Data
        @Accessors(chain = true)
        public static final class ObjectProperty extends Property {
            Map<String, ScalarProperty> values;

            public ObjectProperty(String name) {
                this.name = name;
            }

            public ObjectProperty add(String field, ScalarProperty value) {
                values().put(field, value);
                return this;
            }

            public Map<String, ScalarProperty> values() {
                if (values == null) values = new LinkedHashMap<>();
                return values;
            }
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Variable {
        public static Variable variable(String name) {
            return new Variable().setName(name);
        }

        String name;

        /**
         * A value to use when not provided. Specifying this makes the variable optional
         */
        String defaultValue;

        /**
         * This argument specifies what value types are accepted for the variable.
         */
        VariableType type;

        /**
         * Summary of background information about the variable
         */
        String description;

        // Validation block
        // String validation;

        /**
         * Marks the variable as an encrypted secret to prevent unnecessary display
         */
        Boolean sensitive;

        /**
         * @see jakarta.annotation.Nullable
         */
        @SuppressWarnings("JavadocReference")
        Boolean nullable;
    }
}
