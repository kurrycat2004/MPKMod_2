package io.github.kurrycat.mpkmod.core;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.core.lwjgl.GLStateCacheRedirector;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashSet;
import java.util.Set;

public enum TransformerManager {
    GL_STATE_CACHE_REDIRECTOR(new GLStateCacheRedirector()),
    ;

    private final Transformer transformer;

    TransformerManager(Transformer transformer) {
        this.transformer = transformer;
    }

    private static final Set<Transformer> TRANSFORMERS = new HashSet<>();
    private static volatile boolean initialized = false;

    private static final String MOD_GROUP = Tags.MOD_GROUP.replace('.', '/');
    private static final String EXCLUDE_PREFIX = MOD_GROUP + "/";
    private static final String[] GENERIC_EXCLUDES = {
            "shaded/",
            "core/"
    };

    static {
        for (TransformerManager v : TransformerManager.values()) {
            TRANSFORMERS.add(v.transformer);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static boolean tryInitialize(String source) {
        if (initialized) return false;
        initialized = true;
        Transformer.LOGGER.info("Initializing transformer pipeline using: " + source);
        return true;
    }

    /// Used via reflection as handle in {@link io.github.kurrycat.mpkmod.core.mixin.CoreMixinCoprocessorGenerator}
    @SuppressWarnings("unused")
    public static boolean tryTransform(String className, ClassNode input) {
        if (!shouldTransform(className)) return false;
        return transform(input);
    }

    public static boolean shouldTransform(String className) {
        className = className.replace('.', '/');
        if (className.startsWith(EXCLUDE_PREFIX)) {
            for (String exclude : GENERIC_EXCLUDES) {
                if (className.startsWith(exclude, EXCLUDE_PREFIX.length())) {
                    return false;
                }
            }
        }
        for (Transformer transformer : TRANSFORMERS) {
            if (transformer.shouldTransform(className)) {
                return true;
            }
        }
        return false;
    }

    public static boolean transform(ClassNode input) {
        boolean didTransform = false;

        for (Transformer transformer : TRANSFORMERS) {
            if (!transformer.shouldTransform(input.name)) continue;
            didTransform |= transformer.transform(input);
        }

        return didTransform;
    }
}
