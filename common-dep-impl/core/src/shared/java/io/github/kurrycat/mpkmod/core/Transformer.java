package io.github.kurrycat.mpkmod.core;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import org.objectweb.asm.tree.ClassNode;

public interface Transformer {
    ILogger LOGGER = LogManager.instance().createLogger(Tags.MOD_ID + "/core");

    boolean shouldTransform(String className);

    boolean transform(ClassNode input);
}