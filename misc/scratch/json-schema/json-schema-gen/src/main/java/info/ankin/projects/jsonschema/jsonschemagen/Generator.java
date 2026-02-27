package info.ankin.projects.jsonschema.jsonschemagen;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

public class Generator {
    private static final String JAVA_DOC_START = "/**";
    private static final String JAVA_DOC_CONTINUE = " * ";
    private static final String JAVA_DOC_END = " */";
    private final GeneratorProperties generatorProperties;

    public Generator(GeneratorProperties generatorProperties) {
        this.generatorProperties = generatorProperties;
    }

    String writePojo(PojoDefinition pojoDefinition) {
        StringBuilder stringBuilder = new StringBuilder();

        appendPackage(stringBuilder);

        List<Class<?>> classes = determineClassesForImports(pojoDefinition);
        appendImports(stringBuilder, classes);

        stringBuilder.append(System.lineSeparator());

        stringBuilder.append(JAVA_DOC_START).append(System.lineSeparator());
        stringBuilder.append(JAVA_DOC_CONTINUE).append("This source file was generated").append(System.lineSeparator());
        stringBuilder.append(JAVA_DOC_END).append(System.lineSeparator());
        stringBuilder.append("@Accessors(chain = true)").append(System.lineSeparator());
        stringBuilder.append("@Data").append(System.lineSeparator());
        stringBuilder.append("public class ").append(pojoDefinition.getClassName()).append(" {");
        stringBuilder
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                ;

        boolean appendedFirstField = false;
        for (Map.Entry<String, PojoDefinition.FieldDefinition> field : pojoDefinition.getFields().entrySet()) {
            if (field.getValue().hasComment()) {
                if (appendedFirstField) {
                    stringBuilder.append(System.lineSeparator());
                }

                appendComment(stringBuilder, field);

            }

            stringBuilder
                    .append(generatorProperties.getIndentation())
                    .append("private ")
                    .append(field.getValue().getFieldClass().getSimpleName())
                    .append(" ")
                    .append(field.getKey())
                    .append(";")
                    .append(System.lineSeparator())
            ;
            appendedFirstField = true;
        }

        stringBuilder
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                ;

        stringBuilder.append("}").append(System.lineSeparator());
        return stringBuilder.toString();
    }

    private void appendPackage(StringBuilder stringBuilder) {
        if (generatorProperties.hasPackageName()) {
            stringBuilder.append("package ");
            stringBuilder.append(generatorProperties.getPackageName());
            stringBuilder.append(System.lineSeparator());
        }
    }

    List<Class<?>> determineClassesForImports(PojoDefinition pojoDefinition) {
        Collection<PojoDefinition.FieldDefinition> fieldClasses = pojoDefinition.getFields().values();
        List<Class<?>> classes = fieldClasses.stream()
                .map(PojoDefinition.FieldDefinition::getFieldClass)
                .collect(Collectors.toCollection(ArrayList::new));

        classes.add(Data.class);
        classes.add(Accessors.class);
        classes.sort(Comparator.comparing(Class::getName));
        return classes;
    }

    void appendImports(StringBuilder stringBuilder, List<Class<?>> classes) {
        List<Class<?>> libraryClasses = new ArrayList<>();
        List<Class<?>> builtInClasses = new ArrayList<>();
        for (Class<?> aClass : classes) {
            if (shouldSkip(aClass)) {
                continue;
            }

            List<Class<?>> destination = isBuiltInClass(aClass) ? builtInClasses : libraryClasses;
            destination.add(aClass);
        }

        if (!libraryClasses.isEmpty()) {
            for (Class<?> aClass : libraryClasses) {
                String s = aClass.getName().replaceAll("\\$", ".");
                stringBuilder.append("import ").append(s).append(";").append(System.lineSeparator());
            }

            stringBuilder.append(System.lineSeparator());
        }

        for (Class<?> aClass : builtInClasses) {
            String s = aClass.getName();
            stringBuilder.append("import ").append(s).append(";").append(System.lineSeparator());
        }
    }

    private boolean shouldSkip(Class<?> aClass) {
        return aClass.getName().startsWith("java.lang");
    }

    private boolean isBuiltInClass(Class<?> aClass) {
        return aClass.getName().startsWith("java");
    }

    void appendComment(StringBuilder stringBuilder, Map.Entry<String, PojoDefinition.FieldDefinition> field) {
        List<String> commentLines = commentLines(field.getValue().getComment());

        stringBuilder.append(generatorProperties.getIndentation()).append(JAVA_DOC_START).append(System.lineSeparator());

        for (String commentLine : commentLines) {
            stringBuilder.append(generatorProperties.getIndentation()).append(JAVA_DOC_CONTINUE).append(commentLine).append(System.lineSeparator());
        }

        stringBuilder.append(generatorProperties.getIndentation()).append(JAVA_DOC_END).append(System.lineSeparator());
    }

    List<String> commentLines(String comment) {
        return commentLines(comment,
                generatorProperties.getMaxCommentLineLength() -
                        generatorProperties.getIndentation().length() -
                        3);
    }

    /**
     * @see <a href="https://stackoverflow.com/a/40598783">https://stackoverflow.com/a/40598783</a>
     */
    List<String> commentLines(String comment, int length) {
        String regexString = "\\s*(.{1," + length + "})(?:\\s+|\\s*$)";
        return List.of(comment.replaceAll(regexString, "$1\n").split("\n"));
    }

}
