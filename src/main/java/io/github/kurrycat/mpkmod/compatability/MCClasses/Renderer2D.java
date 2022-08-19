package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.functions.DrawRectFunction;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Renderer2D {
    private static DrawRectFunction drawRectFunction;

    public static void registerDrawRect(DrawRectFunction function) {
        drawRectFunction = function;
    }

    public static void drawRect(Vector2D pos, Vector2D size, Color color) {
        drawRectFunction.apply(pos, size, color);
    }
}
