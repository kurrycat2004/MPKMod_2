package io.github.kurrycat.mpkmod.compatability.functions;

import io.github.kurrycat.mpkmod.compatability.MCClasses.*;

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

    public static void registerPlayButtonSound(PlayButtonSoundFunction function) {
        SoundManager.registerPlayButtonSound(function);
    }

}
