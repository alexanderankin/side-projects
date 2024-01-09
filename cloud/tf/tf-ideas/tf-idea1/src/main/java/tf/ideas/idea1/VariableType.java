package tf.ideas.idea1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

public interface VariableType {
    static ScalarType string() {
        return ScalarType.string;
    }

    static ScalarType number() {
        return ScalarType.number;
    }

    static ScalarType bool() {
        return ScalarType.bool;
    }

    static ListType list(ScalarType type) {
        return new ListType().setType(type);
    }

    static SetType set(ScalarType type) {
        return new SetType().setType(type);
    }

    static ObjectType object() {
        return new ObjectType();
    }

    enum ScalarType implements VariableType {
        string, number, bool
    }

    @Data
    @Accessors(chain = true)
    class Scalar implements VariableType {
        ScalarType type;
    }

    @Data
    @Accessors(chain = true)
    class ListType implements VariableType {
        ScalarType type;
    }

    // ???
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @Data
    @Accessors(chain = true)
    class TupleType extends ListType {
    }

    @Data
    @Accessors(chain = true)
    class SetType implements VariableType {
        ScalarType type;
    }

    @Data
    @Accessors(chain = true)
    class ObjectType implements VariableType {
        Map<String, ScalarType> types;

        @SuppressWarnings("unused")
        public ObjectType withField(String name, ScalarType type) {
            types().put(name, type);
            return this;
        }

        Map<String, ScalarType> types() {
            if (types == null)
                types = new LinkedHashMap<>();
            return types;
        }

    }
}
