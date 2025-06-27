package io.github.kurrycat.mpkmod.core.mixin;

import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.core.Transformer;
import io.github.kurrycat.mpkmod.core.TransformerManager;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

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
            Class<?> coreCoProcClass = Class.forName(
                    "org.spongepowered.asm.mixin.transformer.CoreMixinCoprocessor",
                    true, classLoader);
            Object coreCoProcessor = coreCoProcClass
                    .getConstructor(Class.class)
                    .newInstance(TransformerManager.class);
            Method injectMethod = coreCoProcClass.getMethod("inject");
            injectMethod.invoke(coreCoProcessor);
            TransformerManager.tryInitialize("CoreMixinPlugin");
        } catch (ReflectiveOperationException | ClassCastException e) {
            LOGGER.error("Failed to initialize core mixin plugin", e);
        }
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