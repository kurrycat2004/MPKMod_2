package io.github.kurrycat.mpkmod.core.mixin;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.core.Transformer;
import io.github.kurrycat.mpkmod.core.TransformerManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public final class CoreMixinPlugin implements IMixinConfigPlugin {
    private static final ILogger LOGGER = Transformer.LOGGER.createSubLogger("IMixinConfigPlugin");

    @Override
    public void onLoad(String mixinPackage) {
        if (TransformerManager.isInitialized()) return;
        try {
            ClassLoader classLoader = MixinEnvironment
                    .getCurrentEnvironment()
                    .getActiveTransformer()
                    .getClass().getClassLoader();

            List<?> coprocessors = getCoProcessorList(classLoader);

            Method tryTransformMethod = TransformerManager.class.getMethod("tryTransform", String.class, ClassNode.class);
            MethodHandle tryTransformHandle = MethodHandles.publicLookup().unreflect(tryTransformMethod);

            Class<?> coreCoProcClass = generateCoProcClass(classLoader);

            Object coreCoProcessor = coreCoProcClass
                    .getConstructor(MethodHandle.class)
                    .newInstance(tryTransformHandle);
            injectIntoCoprocessorList(coprocessors, coreCoProcClass, coreCoProcessor);

            TransformerManager.tryInitialize("CoreMixinPlugin");
        } catch (ReflectiveOperationException | ClassCastException e) {
            LOGGER.error("Failed to initialize core mixin plugin", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void injectIntoCoprocessorList(List<T> coprocessors, Class<?> coreCoProcClass, Object coreCoProcessor) throws ClassNotFoundException {
        for (Object coprocessor : coprocessors) {
            if (coreCoProcClass.isInstance(coprocessor)) {
                return;
            }
        }
        coprocessors.add((T) coreCoProcessor);
    }

    private static List<?> getCoProcessorList(ClassLoader classLoader) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Class<?> mixinTransformerClass = Class.forName(
                "org.spongepowered.asm.mixin.transformer.MixinTransformer",
                true, classLoader
        );

        Field procField = mixinTransformerClass.getDeclaredField("processor");
        procField.setAccessible(true);

        Class<?> mixinProcessorClass = Class.forName(
                "org.spongepowered.asm.mixin.transformer.MixinProcessor",
                true, classLoader
        );

        Field coprocsField = mixinProcessorClass.getDeclaredField("coprocessors");
        coprocsField.setAccessible(true);

        Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
        if (!mixinTransformerClass.isInstance(transformer)) {
            throw new ClassCastException(
                    "Active transformer is not an instance of MixinTransformer: " + transformer.getClass()
            );
        }

        Object processor = procField.get(transformer);
        return (List<?>) coprocsField.get(processor);
    }

    private static Class<?> generateCoProcClass(ClassLoader classLoader) throws ReflectiveOperationException {
        byte[] coreCoProcBytecode = CoreMixinCoprocessorGenerator.generate();
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(
                Class.forName("org.spongepowered.asm.mixin.transformer.MixinCoprocessor", true, classLoader),
                MethodHandles.lookup()
        );
        return lookup.defineClass(coreCoProcBytecode);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}