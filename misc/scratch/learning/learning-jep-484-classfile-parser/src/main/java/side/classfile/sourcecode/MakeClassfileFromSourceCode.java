package side.classfile.sourcecode;

import com.fasterxml.jackson.databind.json.JsonMapper;
import groovy.lang.GroovyClassLoader;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.StringReaderSource;
import org.codehaus.groovy.tools.GroovyClass;
import org.springframework.util.FileSystemUtils;
import side.classfile.JsonClassfilePrinter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
public class MakeClassfileFromSourceCode {
    Path tmpDir = getTempDirectory();

    @SneakyThrows
    private static Path getTempDirectory() {
        var tempDirectory = Files.createTempDirectory("MakeClassfileFromSourceCode-");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileSystemUtils.deleteRecursively(tempDirectory);
            } catch (IOException ignored) {
            }
        }));
        return tempDirectory;
    }

    @SneakyThrows
    static void main() {
        var bytes = new MakeClassfileFromSourceCode().classfileFromSourceCode("SomeClass",
                // language=java
                """
                        import groovy.transform.CompileStatic;
                        class SomeClass {
                            @CompileStatic
                            int add(int a, int b) {
                                return a + b;
                            }
                        }
                        """);

        System.out.println(JsonMapper.builder().build().writeValueAsString(JsonClassfilePrinter.classToMethods(bytes)));
    }

    public byte[] classfileFromSourceCode(String fqcn, String source) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setTargetBytecode(CompilerConfiguration.JDK25); // adjust if needed
        config.setTargetDirectory(tmpDir.toFile());

        config.setDebug(true); // -g
        config.setParameters(true); // -parameters

        GroovyClassLoader loader =
                new GroovyClassLoader(getClass().getClassLoader(), config);

        CompilationUnit unit = new CompilationUnit(config, null, loader);

        unit.addSource(new SourceUnit(
                fqcn + ".java", // important: treat as Java
                new StringReaderSource(source, config),
                config,
                loader,
                null
        ));

        unit.compile();

        Map<String, byte[]> classes = unit.getClasses()
                .stream()
                .collect(Collectors.toMap(
                        GroovyClass::getName,
                        GroovyClass::getBytes
                ));

        byte[] bytes = classes.get(fqcn);
        if (bytes == null) {
            throw new IllegalStateException("Generated class not found: " + fqcn);
        }

        return bytes;
    }
}
