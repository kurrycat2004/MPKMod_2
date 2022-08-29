package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.functions.DrawBoxFunction;
import io.github.kurrycat.mpkmod.util.BoundingBox;

import java.awt.*;

public class Renderer3D {
    private static DrawBoxFunction drawBoxFunction;

    public static void registerDrawBoxFunction(DrawBoxFunction f) {
        drawBoxFunction = f;
    }

    public static void drawBox(BoundingBox boundingBox, Color color, Player player, float partialTicks) {
        drawBoxFunction.apply(boundingBox, color, player, partialTicks);
    }
}
