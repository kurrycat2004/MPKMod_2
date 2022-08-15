package io.github.kurrycat.mpkmod.compatability.functions;

import java.awt.*;

@FunctionalInterface
public interface DrawStringFunction {
    void apply(String text, float x, float y, Color color, boolean dropShadow);
}
