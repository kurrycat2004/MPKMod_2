package io.github.kurrycat.mpkmod.compatability.functions;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;

public class FunctionRegistry {
    public static void registerDrawString(DrawStringFunction function) {
        FontRenderer.registerDrawString(function);
    }
}
