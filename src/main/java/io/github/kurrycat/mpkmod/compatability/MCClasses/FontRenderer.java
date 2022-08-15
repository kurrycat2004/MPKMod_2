package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.functions.DrawStringFunction;

import java.awt.*;

public class FontRenderer {
    private static DrawStringFunction drawStringFunction;

    public static void registerDrawString(DrawStringFunction function) {
        drawStringFunction = function;
    }

    public static void drawString(String text, float x, float y, Color color, boolean shadow) {
        drawStringFunction.apply(text, x, y, color, shadow);
    }
}
