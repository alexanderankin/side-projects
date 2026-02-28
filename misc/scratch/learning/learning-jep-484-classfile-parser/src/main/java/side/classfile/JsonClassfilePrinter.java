package side.classfile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.*;
import java.lang.classfile.constantpool.Utf8Entry;
import java.lang.classfile.instruction.*;
import java.lang.constant.ConstantDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class JsonClassfilePrinter {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    static JsonMapper jsonMapper = JsonMapper.builder()
            // .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            // .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
            // .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .build();

    public static void main(String[] args) throws Exception {
        var m = classToMethods(getBytes());
        System.out.println(jsonMapper.writeValueAsString(m));
    }

    public static byte[] getBytes() throws IOException {
        byte[] bytes;
        try (InputStream in =
                     ClassfileParserDemo.class.getResourceAsStream(
                             "/side/classfile/ClassfileParserDemo$ExampleClass.class")) {
            bytes = in.readAllBytes();
        }
        return bytes;
    }

    @SneakyThrows
    public static Map<String, Map<String, Object>> classToMethods(byte[] bytes) {
        ClassModel model = ClassFile.of().parse(bytes);

        var result = new LinkedHashMap<String, Map<String, Object>>();
        for (var method : model.methods()) {
            if (method.methodName().stringValue() != null) {
                var m = methodToJson(method);
                result.put(method.methodName().stringValue(), m);
            }
        }

        return result;
    }

    @SneakyThrows
    static Map<String, Object> methodToJson(MethodModel method) {
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
                        entry.put("raw", InstructionProjector.toJson(insn));
                        return entry;
                    })
                    .toList();
        } else {
            bytecode = List.of();
        }
        json.put("bytecode", bytecode);

        return json;
    }

    static class InstructionProjector {
        public static InstructionJson toJson(Instruction insn) {
            Opcode op = insn.opcode();
            Map<String, Object> args = new LinkedHashMap<>();

            switch (insn) {
                // ILOAD, ILOAD_1, ILOAD_2, ILOAD_3, ALOAD_0
                case LoadInstruction x -> {
                    args.put("typeKind", x.typeKind().toString());
                    args.put("slot", x.slot());   // implicit for _0.._3 forms
                }

                // ISTORE
                case StoreInstruction x -> {
                    args.put("typeKind", x.typeKind().toString());
                    args.put("slot", x.slot());
                }

                // INVOKEVIRTUAL
                case InvokeInstruction x -> {
                    args.put("owner", x.owner().toString());
                    args.put("name", x.name().toString());
                    args.put("descriptor", x.type().toString());
                    args.put("isInterface", x.isInterface());
                    if (x.count() != 0) {
                        args.put("count", x.count()); // only nonzero for invokeinterface
                    }
                }

                // D2I, I2D
                case ConvertInstruction x -> {
                    args.put("from", x.fromType().toString());
                    args.put("to", x.toType().toString());
                }

                // DDIV
                case OperatorInstruction x -> {
                    args.put("typeKind", x.typeKind().toString());
                }

                // ICONST_0, LDC2_W
                case ConstantInstruction x -> {
                    args.put("typeKind", x.typeKind().toString());
                    ConstantDesc c = x.constantValue();
                    args.put("constant", c == null ? null : c.toString());
                }

                // IRETURN
                case ReturnInstruction x -> {
                    args.put("typeKind", x.typeKind().toString());
                }

                default -> {
                    throw new IllegalStateException("Unhandled: " + insn);
                }
            }

            return new InstructionJson(
                    op.toString(),          // e.g. "ILOAD"
                    op.kind().toString(),   // LOAD / STORE / INVOKE / etc.
                    insn.sizeInBytes(),
                    insn.getClass().getInterfaces()[0].getSimpleName(),
                    args
            );
        }

        public record InstructionJson(
                String op,           // e.g. "iload"
                String kind,         // e.g. "LOAD"
                int size,            // bytes
                String type,         // simple instruction interface name
                Map<String, Object> args
        ) {
        }
    }
}
