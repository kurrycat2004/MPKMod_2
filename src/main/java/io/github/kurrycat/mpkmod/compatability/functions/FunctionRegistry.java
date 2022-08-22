package io.github.kurrycat.mpkmod.compatability.functions;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer3D;

public class FunctionRegistry {
    public static void registerDrawString(DrawStringFunction function) {
        FontRenderer.registerDrawString(function);
    }

    public static void registerGetStringSize(GetStringSizeFunction function) {
        FontRenderer.registerGetStringSize(function);
    }

    public static void registerDrawRect(DrawRectFunction function) {
        Renderer2D.registerDrawRect(function);
    }

    public static void registerDrawBox(DrawBoxFunction function) {
        Renderer3D.registerDrawBoxFunction(function);
    }

    public static void registerGetIP(GetIPFunction function) {
        Minecraft.registerGetIPFunction(function);
    }

    public static void registerGetScaledSize(GetScaledSizeFunction function) {
        Renderer2D.registerGetScaledSize(function);
    }

}
