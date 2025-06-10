package io.github.kurrycat.mpkmod.core;

import org.objectweb.asm.tree.ClassNode;

public interface Transformer {
    boolean shouldTransform(String className);

    boolean transform(ClassNode input);
}