package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.functions.DrawRectFunction;
import io.github.kurrycat.mpkmod.compatability.functions.GetScaledSizeFunction;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Renderer2D {
    private static DrawRectFunction drawRectFunction;
    private static GetScaledSizeFunction getScaledSizeFunction;

    public static void registerDrawRect(DrawRectFunction function) {
        drawRectFunction = function;
    }

    public static void registerGetScaledSize(GetScaledSizeFunction f) {
        getScaledSizeFunction = f;
    }

    public static void drawRect(Vector2D pos, Vector2D size, Color color) {
        drawRectFunction.apply(pos, size, color);
    }

    public static Vector2D getScaledSize() {
        return getScaledSizeFunction.apply();
    }
}
