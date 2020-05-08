package pw.dasbrain.nores;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.util.CheckClassAdapter;
import static org.objectweb.asm.Opcodes.*;

import java.util.Set;

import org.objectweb.asm.ClassReader;

public class ClassTransformer extends ClassVisitor {
    
    public ClassTransformer(ClassVisitor parent) {
        super(ASM8, parent);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        access &= ~(ACC_STATIC);
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    static byte[] transform(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, 0);
        // Node - Class visitors are stacked upside down - the last visitor in this code will run first
        ClassVisitor cv = new CheckClassAdapter(cw);
        // Last we change the names for some methods and classes.
        cv = new ClassRemapper(cv, NorrisRemapper.INSTANCE);
        // We replace the contents of those Strings.get calls
        cv = new StringsMethodRemover(cv);
        // Replace those stupid invokedynamic calls
        cv = new ClassTransformer(cv);
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
            String signature, String[] exceptions) {
        MethodVisitor parent = super.visitMethod(access, name, descriptor, signature,
                exceptions);
        return new MethodTransformer(parent);
    }
    
}

class MethodTransformer extends MethodVisitor {
    
    static final Set<String> INTERFACES = Set.of("t", "ck", "aq");
    
    public MethodTransformer(MethodVisitor parent) {
        super(ASM8, parent);
    }
    
    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor,
            Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        if (bootstrapMethodHandle.getName().equals("bootstrap$0")) {
            int opcode = (Integer) bootstrapMethodArguments[0];
            String desc = (String) bootstrapMethodArguments[3];
            String owner = (String) bootstrapMethodArguments[1];
            String mname = (String) bootstrapMethodArguments[2];
            
            boolean isInterface;
            if (owner.lastIndexOf('.') != -1) {
                try {
                    isInterface = Class.forName(owner).isInterface();
                } catch (ClassNotFoundException e) {
                    throw new Error(e);
                }
            } else {
                isInterface = INTERFACES.contains(owner);
            }
            super.visitMethodInsn(opcode, owner.replace('.', '/'), mname, desc, isInterface);
            return;
        }
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle,
                bootstrapMethodArguments);
    }
}