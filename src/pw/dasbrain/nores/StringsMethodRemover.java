package pw.dasbrain.nores;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

public class StringsMethodRemover extends ClassVisitor {
    
    private static final Map<Integer, String> AGENT_1_STRINGS = Map.of(3104478, "fail",
            85, "V", -1437791514, "assert", -1318747143, "expect");
    private static final Map<Integer, String> AGENT_STRINGS = Map.of(1673725702,
            "java.class.path", 106148, "lib", 1444589, ".jar", 1382100394, ".class",
            -494889092, "path.separator", -2060416996, "org/junit/Assert.class",
            -1741207126, "java.home", 45, ".", 46, "/", -892381648, "org.junit.Assert");
    
    private final ClassVisitor parent;
    private final ClassNode node;
    
    public StringsMethodRemover(ClassVisitor parent) {
        this(parent, new ClassNode());
    }
    
    private StringsMethodRemover(ClassVisitor parent, ClassNode node) {
        super(ASM8, node);
        this.parent = parent;
        this.node = node;
    }
    
    @Override
    public void visitEnd() {
        super.visitEnd();
        node.methods.forEach(StringsMethodRemover::transformMethod);
        node.accept(parent);
    }
    
    private static void transformMethod(MethodNode node) {
        ListIterator<AbstractInsnNode> iter = node.instructions.iterator();
        
        while (iter.hasNext()) {
            var isn = iter.next();
            if (isn.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode min = (MethodInsnNode) isn;
                if (min.name.equals("get")) {
                    
                    Map<Integer, String> map = switch (min.owner) {
                        case "Agent$Strings" -> AGENT_STRINGS;
                        case "Agent$1$Strings" -> AGENT_1_STRINGS;
                        default -> null;
                    };
                    if (map != null) {
                        iter.previous();
                        iter.remove();
                        var prev = (LdcInsnNode) iter.previous();
                        prev.cst = map.get(prev.cst);
                    }
                }
            }
        }
    }
}
