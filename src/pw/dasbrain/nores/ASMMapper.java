package pw.dasbrain.nores;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.objectweb.asm.commons.Remapper;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ASMMapper extends Remapper {
    
    public static ASMMapper INSTANCE = new ASMMapper();
    
    private static Map<String, String> SIGNATURES = new HashMap<>();
    private static Map<String, String> CLASS_NAME_MAP = new HashMap<>();
    private static Map<String, List<TypeAndName>> METHOD_LISTS = new HashMap<>();
    private static Map<String, List<TypeAndName>> FIELD_LISTS = new HashMap<>();
    private static Map<String, Map<TypeAndName, String>> METHOD_MAP = new HashMap<>();
    private static Map<String, Map<TypeAndName, String>> FIELD_MAP = new HashMap<>();
    
    private static Map<String, List<String>> SUPERCLASSES = new HashMap<>();
    
    static void init(Path asmfile) {
        try (var fs = FileSystems.newFileSystem(asmfile)) {
            initPath(fs.getPath("/"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    static void initPath(Path root) {
        try (var files = Files.find(root, 30,
                (p, a) -> p.toString().endsWith(".class"))) {
            files.forEach(ASMMapper::makeASM);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (var fs = FileSystems.newFileSystem(Path.of("codesnores.jar"));
                var files = Files.find(fs.getPath("/"), 30,
                        (p, a) -> p.toString().endsWith(".class"))) {
            files.forEach(ASMMapper::makeNores);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    static void makeASM(Path p) {
        if (p.endsWith("module-info.class"))
            return;
        try {
            byte[] classFile = Files.readAllBytes(p);
            ClassReader cr = new ClassReader(classFile);
            SignatureGenerator sig = new SignatureGenerator();
            cr.accept(sig, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
                    | ClassReader.SKIP_FRAMES);
            
            String cn = cr.getClassName();
            FIELD_LISTS.put(cn, sig.fields());
            METHOD_LISTS.put(cn, sig.methods());
            SIGNATURES.put(sig.toString(), cn);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    static void makeNores(Path p) {
        try {
            byte[] classFile = Files.readAllBytes(p);
            ClassReader cr = new ClassReader(classFile);
            SignatureGenerator sig = new SignatureGenerator();
            cr.accept(sig, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
                    | ClassReader.SKIP_FRAMES);
            
            String mangled = cr.getClassName();
            
            List<String> superClasses = new ArrayList<>();
            superClasses.add(cr.getSuperName());
            superClasses.addAll(Arrays.asList(cr.getInterfaces()));
            SUPERCLASSES.put(mangled, superClasses);
            
            String orig = SIGNATURES.get(sig.toString());
            if (orig != null) {
                CLASS_NAME_MAP.put(mangled, orig);
                FIELD_MAP.put(mangled,
                        createMapping(FIELD_LISTS.get(orig), sig.fields()));
                METHOD_MAP.put(mangled,
                        createMapping(METHOD_LISTS.get(orig), sig.methods()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private static Map<TypeAndName, String> createMapping(List<TypeAndName> orig,
            List<TypeAndName> mangled) {
        var map = new HashMap<TypeAndName, String>();
        int size = orig.size();
        assert size == mangled.size();
        for (int i = 0; i < size; i++) {
            map.put(mangled.get(i), orig.get(i).name());
        }
        return map;
    }
    
    @Override
    public String map(String internalName) {
        return CLASS_NAME_MAP.getOrDefault(internalName, internalName);
    }
    
    static String findName(Map<String, Map<TypeAndName, String>> map, String owner, String name, String descriptor) {
        List<String> toSearch;
        List<String> nextSearch = List.of(owner);
        TypeAndName key = new TypeAndName(descriptor, name);
        while (!nextSearch.isEmpty()) {
            toSearch = nextSearch;
            nextSearch = new ArrayList<>();
            for (String clname : toSearch) {
                List<String> supers = SUPERCLASSES.get(clname);
                if (supers == null) continue;
                String result = map.getOrDefault(clname, Map.of()).get(key);
                if (result != null) return result;
                nextSearch.addAll(supers);
            }
        }
        return name;
    }
    
    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        return findName(FIELD_MAP, owner, name, descriptor);
    }
    
    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        return findName(METHOD_MAP, owner, name, descriptor);
    }
    
    public static void main(String[] args) {
        init(Path.of("ASM-5.0.4-noshrink.zip"));
        System.out.println(CLASS_NAME_MAP);
    }
}

class SignatureGenerator extends ClassVisitor {
    private final StringBuilder sb = new StringBuilder();
    private final List<TypeAndName> fields = new ArrayList<>();
    private final List<TypeAndName> methods = new ArrayList<>();
    
    SignatureGenerator() {
        super(ASM8);
    }
    
    static String blankType(String desc) {
        return blankType(Type.getType(desc)).getDescriptor();
    }
    
    private static final Predicate<String> UNNAMED_PACKAGE = Pattern
            .compile("L[0-9a-zA-Z\\$]+;").asMatchPredicate();
    private static final Type BLANK_TYPE = Type.getType("LX;");
    
    static Type blankType(Type arg) {
        return switch (arg.getSort()) {
            case Type.ARRAY -> Type
                    .getType("[" + blankType(arg.getDescriptor().substring(1)));
            case Type.OBJECT -> {
                var desc = arg.getDescriptor();
                yield desc.startsWith("Lorg/") || UNNAMED_PACKAGE.test(desc) ? BLANK_TYPE
                        : arg;
            }
            default -> arg;
        };
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor,
            String signature, Object value) {
        fields.add(new TypeAndName(descriptor, name));
        sb.append("F").append(blankType(descriptor));
        return null;
    }
    
    static String blankMethod(String desc) {
        Type m = Type.getType(desc);
        Type ret = blankType(m.getReturnType());
        Type[] args = Arrays.stream(m.getArgumentTypes())
                .map(SignatureGenerator::blankType).toArray(Type[]::new);
        return Type.getMethodDescriptor(ret, args);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
            String signature, String[] exceptions) {
        if (name.equals("bootstrap$0")) return null;
        methods.add(new TypeAndName(descriptor, name));
        sb.append("M").append(blankMethod(descriptor));
        return null;
    }
    
    List<TypeAndName> methods() {
        return methods;
    }
    
    List<TypeAndName> fields() {
        return fields;
    }
    
    @Override
    public String toString() {
        return sb.toString();
    }
}