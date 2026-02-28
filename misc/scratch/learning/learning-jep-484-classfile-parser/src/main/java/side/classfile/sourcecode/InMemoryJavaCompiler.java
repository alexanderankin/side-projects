package side.classfile.sourcecode;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import side.classfile.JsonClassfilePrinter;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryJavaCompiler {

    @SneakyThrows
    static void main() {
        System.out.println(JsonMapper.builder().build().writeValueAsString(JsonClassfilePrinter.classToMethods(compile("Example", """
                int add(int a, int b) {
                    return a + b;
                }
                int average(int a, int b, int c) {
                    int sum = 0;
                    sum = add(sum, a);
                    sum = add(sum, b);
                    sum = add(sum, c);
                    return (int) (sum / 3.0);
                }
                """))));
    }

    @SneakyThrows
    public static byte[] compile(String className, String methodSource) {

        String fullSource = """
                public class %s {
                    %s
                }
                """.formatted(className, methodSource);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No system compiler. Use a JDK, not a JRE.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        StandardJavaFileManager stdManager =
                compiler.getStandardFileManager(diagnostics, null, null);

        try (MemoryFileManager fileManager = new MemoryFileManager(stdManager)) {

            JavaFileObject sourceFile = new MemorySource(className, fullSource);

            List<String> options = List.of(
                    "-parameters",   // method parameter metadata
                    "-g"             // full debug info
            );

            JavaCompiler.CompilationTask task =
                    compiler.getTask(
                            null,
                            fileManager,
                            diagnostics,
                            options,
                            null,
                            List.of(sourceFile)
                    );

            boolean success = task.call();
            if (!success) {
                StringBuilder sb = new StringBuilder();
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    sb.append(d).append("\n");
                }
                throw new IllegalStateException(sb.toString());
            }

            return fileManager.getClassBytes(className);
        }
    }

    // ---------------- In-memory source ----------------

    static final class MemorySource extends SimpleJavaFileObject {
        private final String code;

        MemorySource(String className, String code) {
            super(URI.create("string:///" + className + ".java"), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    static final class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, ByteArrayOutputStream> compiled = new HashMap<>();

        MemoryFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compiled.put(className, baos);

            return new MySimpleJavaFileObject(className, kind, baos);
        }

        byte[] getClassBytes(String className) {
            ByteArrayOutputStream baos = compiled.get(className);
            if (baos == null) {
                throw new IllegalStateException("Class not generated: " + className);
            }
            return baos.toByteArray();
        }

        private static class MySimpleJavaFileObject extends SimpleJavaFileObject {
            private final ByteArrayOutputStream baos;

            public MySimpleJavaFileObject(String className, Kind kind, ByteArrayOutputStream baos) {
                super(URI.create("mem:///" + className + kind.extension), kind);
                this.baos = baos;
            }

            @Override
            public OutputStream openOutputStream() {
                return baos;
            }
        }
    }
}
