package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.functions.DrawStringFunction;
import io.github.kurrycat.mpkmod.compatability.functions.GetStringSizeFunction;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class FontRenderer {
    private static DrawStringFunction drawStringFunction;
    private static GetStringSizeFunction getStringSizeFunction;

    public static void registerDrawString(DrawStringFunction function) {
        drawStringFunction = function;
    }

    public static void registerGetStringSize(GetStringSizeFunction f) {
        getStringSizeFunction = f;
    }

    public static void drawString(String text, Vector2D pos, Color color, boolean shadow) {
        drawStringFunction.apply(text, pos, color, shadow);
    }

    public static void drawCenteredString(String text, Vector2D pos, Color color, boolean shadow) {
        drawString(text, pos.sub(getStringSize(text).div(2)), color, shadow);

    }

    public static Vector2D getStringSize(String text) {
        return getStringSizeFunction.apply(text);
    }
}
