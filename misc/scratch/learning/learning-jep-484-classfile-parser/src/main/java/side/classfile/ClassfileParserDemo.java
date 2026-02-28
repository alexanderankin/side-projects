package side.classfile;

import lombok.SneakyThrows;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;

public class ClassfileParserDemo {
    @SneakyThrows
    static void main() {
        Class<?> ourClass = ClassfileParserDemo.class;
        System.out.println("/" + ourClass.getName().replace(".", "/") + ".class");
        System.out.println(ourClass.getSimpleName() + ".class");
        System.out.println(ExampleClass.class.getSimpleName() + ".class");
        System.out.println(ExampleClass.class.getDeclaringClass().getName());
        System.out.println(ExampleClass.class.getCanonicalName());
        System.out.println(ourClass.getResourceAsStream(ExampleClass.class.getSimpleName() + ".class"));

        var bytes = ClassfileParserDemo.class.getResourceAsStream("/side/classfile/ClassfileParserDemo$ExampleClass.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(bytes);
        for (var method : cm.methods()) {
            System.out.println(method.methodName().stringValue());
        }
    }

    static class ExampleClass {
        public int add(int a, int b) {
            return a + b;
        }
    }
}
