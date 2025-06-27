package io.github.kurrycat.mpkmod.core.mixin;

import io.github.kurrycat.mpkmod.Tags;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Results in:
 * <pre>{@code
 * public class CoreMixinCoprocessor extends MixinCoprocessor {
 *     private final MethodHandle handle;
 *
 *     public CoreMixinCoprocessor(MethodHandle handle) {
 *         this.handle = handle;
 *     }
 *
 *     @Override
 *     String getName() {
 *         return Tags.MOD_ID + "-core-transformer";
 *     }
 *
 *     @Override
 *     ProcessResult process(String name, ClassNode node) {
 *         try {
 *             return (boolean) handle.invokeExact(name, node)
 *                    ? ProcessResult.TRANSFORMED
 *                    : ProcessResult.NONE;
 *         } catch (Throwable t) {
 *             throw new RuntimeException(t);
 *         }
 *     }
 * }
 * }</pre>
 */
public class CoreMixinCoprocessorGenerator implements Opcodes {
    public static byte[] generate() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        String className = "org/spongepowered/asm/mixin/transformer/CoreMixinCoprocessor";
        String superName = "org/spongepowered/asm/mixin/transformer/MixinCoprocessor";

        cw.visit(V1_8, ACC_PUBLIC, className, null, superName, null);

        // Inner class ProcessResult
        cw.visitInnerClass("org/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult",
                "org/spongepowered/asm/mixin/transformer/MixinCoprocessor",
                "ProcessResult", ACC_FINAL | ACC_STATIC | ACC_ENUM);

        /*
        private final MethodHandle handle;
        */
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "handle", "Ljava/lang/invoke/MethodHandle;", null, null).visitEnd();

         /*
         public CoreMixinCoprocessor(MethodHandle handle) {
             super();
             this.handle = handle;
         }
         */
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/invoke/MethodHandle;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0); // this
        mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, className, "handle", "Ljava/lang/invoke/MethodHandle;");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        /*
        @Override
        protected String getName() {
            return "core-transformer";
        }
        */
        mv = cw.visitMethod(ACC_PROTECTED, "getName", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(Tags.MOD_ID + "-core-transformer");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        /*
        @Override
        public MixinCoprocessor.ProcessResult process(String className, ClassNode classNode) {
            try {
                return handle.invokeExact(className, classNode)
                        ? MixinCoprocessor.ProcessResult.TRANSFORMED
                        : MixinCoprocessor.ProcessResult.NONE;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
         */
        mv = cw.visitMethod(ACC_PUBLIC, "process",
                "(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Lorg/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult;",
                null, null);
        mv.visitCode();

        Label tryStart = new Label();
        Label tryEnd = new Label();
        Label catchBlock = new Label();

        mv.visitTryCatchBlock(tryStart, tryEnd, catchBlock, "java/lang/Throwable");

        mv.visitLabel(tryStart);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, className, "handle", "Ljava/lang/invoke/MethodHandle;");
        mv.visitVarInsn(ALOAD, 1); // className
        mv.visitVarInsn(ALOAD, 2); // classNode
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact",
                "(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Z", false);

        Label elseLabel = new Label();
        mv.visitJumpInsn(IFEQ, elseLabel);

        mv.visitFieldInsn(GETSTATIC,
                "org/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult",
                "TRANSFORMED",
                "Lorg/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult;");
        Label returnLabel = new Label();
        mv.visitJumpInsn(GOTO, returnLabel);

        mv.visitLabel(elseLabel);
        mv.visitFieldInsn(GETSTATIC,
                "org/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult",
                "NONE",
                "Lorg/spongepowered/asm/mixin/transformer/MixinCoprocessor$ProcessResult;");

        mv.visitLabel(returnLabel);
        mv.visitInsn(ARETURN);
        mv.visitLabel(tryEnd);

        // Catch block
        mv.visitLabel(catchBlock);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V", false);
        mv.visitInsn(ATHROW);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // public synthetic bridge onInit(MixinInfo)
        mv = cw.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC, "onInit",
                "(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, superName, "onInit",
                "(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        // public synthetic bridge onPrepare(MixinInfo)
        mv = cw.visitMethod(ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC, "onPrepare",
                "(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, superName, "onPrepare",
                "(Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}