package io.github.kurrycat.mpkmod.util;

import java.awt.*;

public class ColorUtil {
    public static Color fadeColor(Color c, double multiplier) {
        //Text shows as alpha 255 when alpha is < 4 in 1.8 for whatever reason
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(4, (int) (c.getAlpha() * multiplier)));
    }

    public static Color withAlpha(Color c, double alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(4, (int) alpha));
    }
}
