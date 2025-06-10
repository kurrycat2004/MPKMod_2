package io.github.kurrycat.mpkmod.core.fml;

import io.github.kurrycat.mpkmod.core.TransformerManager;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/// Used in {@link CoreLoadingPlugin#getASMTransformerClass()}
public final class CoreTransformer implements IClassTransformer {
    private static final boolean hasInitialized = TransformerManager.tryInitialize("IFMLLoadingPlugin");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!hasInitialized || basicClass == null) return basicClass;

        if (!TransformerManager.shouldTransform(transformedName)) {
            return basicClass;
        }

        ClassReader reader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        boolean changed = TransformerManager.transform(classNode);
        if (!changed) return basicClass;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}