package info.ankin.projects.jsonschema.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TypeFieldStringOrArrayTest {
    ObjectMapper objectMapper;

    /**
     * Custom deserializer which handles deserializing the {@link Representation1#getType()} field.
     * <p>
     * Allows for single or multiple {@link SimpleType} values.
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new SimpleModule()
                .addDeserializer(Representation.class, new JsonDeserializer<>() {
                    @Override
                    public Representation deserialize(JsonParser p, DeserializationContext c)
                            throws IOException {
                        TreeNode treeNode = p.getCodec().readTree(p);

                        // if we are a string, then it's the only one, otherwise it is an array
                        if (treeNode instanceof TextNode)
                            return objectMapper.treeToValue(treeNode, Representation1.class);
                        else
                            return objectMapper.treeToValue(treeNode, Representation2.class);
                    }
                }));
    }

    /**
     * This test represents a token which is a type, consisting of just a single type
     */
    @Test
    void test1() {
        String input1 = "\"ENUM\"";

        Representation output1 = parse(input1);
        System.out.println("output1: " + output1);

        assertThat(output1, is(instanceOf(Representation1.class)));
        assertThat(((Representation1) output1).getType(), is(SimpleType.ENUM));
    }

    /**
     * This test represents a token which is a type, consisting of list of types
     */
    @Test
    void test2() {
        String input2 = "[\"ENUM\", \"ARRAY\"]";
        Representation output2 = parse(input2);
        System.out.println("output2: " + output2);
        assertThat(output2, is(instanceOf(Representation2.class)));
        assertThat(((Representation2) output2).getTypes(),
                contains(SimpleType.ENUM, SimpleType.ARRAY));
    }

    @SneakyThrows
    @Test
    void test_parseAsField() {
        String input = "[{\"name\":\"field0\",\"type\":\"BOOLEAN\"}," +
                "{\"name\":\"field1\",\"type\":[\"ARRAY\",\"BOOLEAN\"]}]";
        Field[] field = objectMapper.readValue(input, Field[].class);
        assertThat(field.length, is(2));
        assertThat(field[0].getName(), is("field0"));
        assertThat(field[0].getType(), is(instanceOf(Representation1.class)));
        assertThat(((Representation1) field[0].getType()).getType(),
                is(SimpleType.BOOLEAN));

        assertThat(field[1].getName(), is("field1"));
        assertThat(field[1].getType(), is(instanceOf(Representation2.class)));
        assertThat(((Representation2) field[1].getType()).getTypes(),
                contains(SimpleType.ARRAY, SimpleType.BOOLEAN));
    }

    @SneakyThrows
    private Representation parse(String input2) {
        return objectMapper.readValue(input2, Representation.class);
    }

    /**
     * a type of object in the JSON Schema V4 meta-schema.
     *
     * @see <a href="https://github.com/json-schema-org/json-schema-spec/blob/dba92b702c94858162f653590230e7573c8b7dd0/schema.json#L19">"simpleTypes" in v4 meta-schema</a>
     */
    public enum SimpleType {
        ARRAY,
        BOOLEAN,
        INTEGER,
        NULL,
        NUMBER,
        OBJECT,
        STRING,
        ENUM
    }

    @Data
    public static class Field {
        String name;
        Representation type;
    }

    /**
     * Representation of the type field, which can be a type or a list of types
     *
     * @see <a href="https://github.com/json-schema-org/json-schema-spec/blob/dba92b702c94858162f653590230e7573c8b7dd0/schema.json#L127-L137">"type" field in v4 meta-schema</a>
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonSubTypes(value = {
            @JsonSubTypes.Type(Representation1.class),
            @JsonSubTypes.Type(Representation2.class)
    })
    public interface Representation {
    }

    @Data
    public static class Representation1 implements Representation {
        SimpleType type;

        @JsonCreator
        public Representation1(SimpleType type) {
            this.type = type;
        }
    }

    @Data
    public static class Representation2 implements Representation {
        List<SimpleType> types;

        @JsonCreator
        public Representation2(List<SimpleType> types) {
            this.types = types;
        }
    }
}
