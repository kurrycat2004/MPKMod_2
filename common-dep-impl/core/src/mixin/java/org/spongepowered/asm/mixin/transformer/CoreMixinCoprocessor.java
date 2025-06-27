package org.spongepowered.asm.mixin.transformer;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.core.mixin.CoreMixinPlugin;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/// Instantiated using reflection in {@link CoreMixinPlugin#onLoad(String)}
@SuppressWarnings("unused")
public class CoreMixinCoprocessor extends MixinCoprocessor {
    private final MethodHandle tryTransformHandle;

    public CoreMixinCoprocessor(Class<?> transformerClass) throws ReflectiveOperationException {
        Method tryTransform = transformerClass.getMethod("tryTransform", String.class, ClassNode.class);
        tryTransformHandle = MethodHandles.publicLookup().unreflect(tryTransform);
    }

    @Override
    String getName() {
        return Tags.MOD_ID + "-core-transformer";
    }

    /// Called using reflection in {@link CoreMixinPlugin#onLoad(String)}
    @SuppressWarnings("unused")
    public void inject() throws NoSuchFieldException, ClassCastException, IllegalAccessException {
        Field procField = MixinTransformer.class.getDeclaredField("processor");
        procField.setAccessible(true);

        Field coprocsField = MixinProcessor.class.getDeclaredField("coprocessors");
        coprocsField.setAccessible(true);

        MixinTransformer transformer = (MixinTransformer) MixinEnvironment
                .getCurrentEnvironment()
                .getActiveTransformer();

        MixinProcessor processor = (MixinProcessor) procField.get(transformer);
        MixinCoprocessors coprocessors = (MixinCoprocessors) coprocsField.get(processor);

        for (MixinCoprocessor coprocessor : coprocessors) {
            if (coprocessor instanceof CoreMixinCoprocessor) {
                return;
            }
        }
        coprocessors.add(this);
    }

    @Override
    final ProcessResult process(String className, ClassNode classNode) {
        boolean didTransform;
        try {
            didTransform = (boolean) tryTransformHandle.invokeExact(className, classNode);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return didTransform ? ProcessResult.TRANSFORMED : ProcessResult.NONE;
    }
}
