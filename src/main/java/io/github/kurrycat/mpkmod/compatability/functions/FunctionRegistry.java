package io.github.kurrycat.mpkmod.compatability.functions;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;

public class FunctionRegistry {
    public static void registerDrawString(DrawStringFunction function) {
        FontRenderer.registerDrawString(function);
    }

    public static void registerGetIP(GetIPFunction function) {
        Minecraft.registerGetIPFunction(function);
    }
}
