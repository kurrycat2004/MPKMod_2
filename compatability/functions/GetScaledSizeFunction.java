package io.github.kurrycat.mpkmod.compatability.functions;

import io.github.kurrycat.mpkmod.util.Vector2D;

@FunctionalInterface
public interface GetScaledSizeFunction {
    Vector2D apply();
}
