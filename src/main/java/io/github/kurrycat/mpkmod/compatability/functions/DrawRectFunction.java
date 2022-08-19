package io.github.kurrycat.mpkmod.compatability.functions;

import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

@FunctionalInterface
public interface DrawRectFunction {
    void apply(Vector2D pos, Vector2D size, Color color);
}
