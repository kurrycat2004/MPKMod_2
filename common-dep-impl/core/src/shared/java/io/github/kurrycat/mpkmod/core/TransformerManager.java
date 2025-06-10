package io.github.kurrycat.mpkmod.core;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
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

    public static final ILogger LOGGER = LogManager.instance().createLogger(Tags.MOD_ID + "/core");
    private static final Set<Transformer> TRANSFORMERS = new HashSet<>();
    private static volatile boolean initialized = false;

    static {
        for (TransformerManager v : TransformerManager.values()) {
            TRANSFORMERS.add(v.transformer);
        }
    }

    public static boolean tryInitialize(String source) {
        if (initialized) return false;
        initialized = true;
        LOGGER.info("Initializing transformer pipeline using: " + source);
        return true;
    }

    public static boolean shouldTransform(String className) {
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
