package side.classfile;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.constant.MethodTypeDesc;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class JsonClassfilePrinter {

    public static void main(String[] args) throws Exception {
        byte[] bytes;

        try (InputStream in =
                     ClassfileParserDemo.class.getResourceAsStream(
                             "/side/classfile/ClassfileParserDemo$ExampleClass.class")) {
            bytes = in.readAllBytes();
        }

        ClassModel model = ClassFile.of().parse(bytes);

        for (var method : model.methods()) {
            if (method.methodName().stringValue().equals("add")) {
                printMethodAsJson(method);
            }
        }
    }

    @SneakyThrows
    static void printMethodAsJson(MethodModel method) {
        MethodTypeDesc signatureInformation = method.methodTypeSymbol();

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("function_name", method.methodName().stringValue());
        json.put("argcount", signatureInformation.parameterCount());

        var paramNames = method.findAttribute(Attributes.methodParameters())
                .map(attribute -> attribute.parameters().stream()
                        .map(p -> p.name()
                                .map(Utf8Entry::stringValue)
                                .orElse("<unnamed>"))
                        .toList())
                .orElse(null);

        json.put("parameters", IntStream.range(0, signatureInformation.parameterCount())
                .mapToObj(parameterIndex -> {
                    var classDesc = signatureInformation.parameterType(parameterIndex);

                    var parameterAsMap = new LinkedHashMap<String, Object>();
                    parameterAsMap.put("type", classDesc.displayName());
                    parameterAsMap.put("primitive", classDesc.isPrimitive());
                    if (paramNames != null)
                        parameterAsMap.put("name", paramNames.get(parameterIndex));
                    return parameterAsMap;
                })
                .toList());


        List<Map<String, Object>> bytecode;
        var codeElements = method.findAttribute(Attributes.code()).orElse(null);
        if (codeElements != null) {
            bytecode = codeElements.elementList().stream()
                    .filter(Instruction.class::isInstance)
                    .map(Instruction.class::cast)
                    .map(insn -> {
                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("opname", insn.opcode().name());
                        entry.put("opcode", insn.opcode().bytecode());
                        return entry;
                    })
                    .toList();
        } else {
            bytecode = List.of();
        }
        json.put("bytecode", bytecode);

        System.out.println(JsonMapper.builder().build().writeValueAsString(json));
    }
}
