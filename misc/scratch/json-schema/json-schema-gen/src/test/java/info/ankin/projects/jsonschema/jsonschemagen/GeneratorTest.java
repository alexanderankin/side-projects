package info.ankin.projects.jsonschema.jsonschemagen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GeneratorTest {
    GeneratorProperties generatorProperties;
    Generator generator;

    @BeforeEach
    void setup() {
        generatorProperties = new GeneratorProperties();
        generator = new Generator(generatorProperties);
    }

    @Test
    void test() {

        PojoDefinition pojoDefinition = new PojoDefinition().setClassName("Abc")
                .append("id", new PojoDefinition.FieldDefinition().setFieldClass(UUID.class))
                .append("abc", new PojoDefinition.FieldDefinition().setFieldClass(String.class))
                .append("ex", new PojoDefinition.FieldDefinition().setFieldClass(Ex.class))
                .append("number", new PojoDefinition.FieldDefinition().setFieldClass(Integer.class).setComment("abc"));

        String s = generator.writePojo(pojoDefinition);
        System.out.println(s);
    }

    static class Ex {
    }

    @Test
    void test_wrappingComments() {
        assertThat(generator.commentLines("abc"), is(List.of("abc")));
        assertThat(generator.commentLines("Laborum fugiat sed nostrud enim aliquip incididunt duis elit exercitation\n" +
                        "adipisicing ut proident sunt sit nisi ea cillum tempor ut in elit fugiat\n" +
                        "occaecat ad labore in sint consequat ut irure do esse irure dolor non enim\n" +
                        "dolor voluptate veniam et non deserunt ex esse excepteur ut consectetur\n" +
                        "laborum reprehenderit esse consequat esse ex sint eiusmod incididunt nisi\n" +
                        "mollit magna ut id fugiat nisi nostrud consectetur proident qui cillum nulla\n" +
                        "sint incididunt ut id elit cupidatat tempor qui cillum elit magna in veniam"),
                is(
                        List.of("Laborum fugiat sed nostrud enim aliquip incididunt duis elit exercitation",
                                "adipisicing ut proident sunt sit nisi ea cillum tempor ut in elit fugiat",
                                "occaecat ad labore in sint consequat ut irure do esse irure dolor non enim",
                                "dolor voluptate veniam et non deserunt ex esse excepteur ut consectetur",
                                "laborum reprehenderit esse consequat esse ex sint eiusmod incididunt nisi",
                                "mollit magna ut id fugiat nisi nostrud consectetur proident qui cillum",
                                "nulla",
                                "sint incididunt ut id elit cupidatat tempor qui cillum elit magna in",
                                "veniam")
                ));

    }

}
