package io.github.kurrycat.mpkmod.compatability.functions;

import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

@FunctionalInterface
public interface DrawStringFunction {
    void apply(String text, Vector2D pos, Color color, boolean dropShadow);
}
