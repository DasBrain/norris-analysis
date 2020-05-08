package pw.dasbrain.nores;

import org.objectweb.asm.commons.Remapper;

public class NorrisRemapper extends Remapper {
    
    private NorrisRemapper() {
    }
    
    public static final NorrisRemapper INSTANCE = new NorrisRemapper();
    
    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        return switch (owner) {
            case "Agent" -> switch (name) {
                case "a" -> switch (descriptor) {
                    case "(Ljava/io/InputStream;)[B" -> "readAllBytes";
                    case "([B)[B" -> "transform";
                    case "(Ljava/io/File;Ljava/io/File;)Ljava/lang/String;" -> "className";
                    case "(Ljava/util/Map;ZLjava/util/function/Predicate;)V" -> "scanClasspath";
                    case "(Ljava/io/File;Ljava/io/File;Ljava/util/Map;Ljava/util/function/Predicate;)Z" -> "scanJarOrDir";
                    default -> name;
                };
                default -> name;
            };
            case "h" -> switch (name) {
                case "b" -> switch (descriptor) {
                    case "()[B" -> "toByteArray";
                    default -> name;
                };
                default -> name;
            };
            default -> name;
        };
    }
    
    // Some classes are from ASM5.
    @Override
    public String map(String internalName) {
        return switch (internalName) {
            case "a" -> "AgentClassInfo";
            case "t" -> "Opcodes";
            case "f" -> "ClassReader";
            case "h" -> "ClassWriter";
            case "g" -> "ClassVisitor";
            case "bq" -> "MethodNode";
            case "r" -> "MethodVisitor";
            case "ar" -> "AgentMethodNode";
            default -> internalName;
        };
    }
}
