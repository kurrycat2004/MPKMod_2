package io.github.kurrycat.mpkmod.util;

public final class MathUtil {
    private MathUtil() {}

    public static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(value, min));
    }
}
