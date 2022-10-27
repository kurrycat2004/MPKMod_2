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

    public static Color hexToColor(String hex) {
        if (hex.length() != 9 || !hex.startsWith("#")) return null;

        int red = Integer.parseInt(hex.substring(1, 3), 16);
        int green = Integer.parseInt(hex.substring(3, 5), 16);
        int blue = Integer.parseInt(hex.substring(5, 7), 16);
        int alpha = Integer.parseInt(hex.substring(7), 16);

        return new Color(red, green, blue, alpha);
    }

    public static String colorToHex(Color c) {
        String red = Integer.toHexString(c.getRed());
        String green = Integer.toHexString(c.getGreen());
        String blue = Integer.toHexString(c.getBlue());
        String alpha = Integer.toHexString(c.getAlpha());

        if (red.length() == 1) red = "0" + red;
        if (green.length() == 1) green = "0" + green;
        if (blue.length() == 1) blue = "0" + blue;
        if (alpha.length() == 1) alpha = "0" + alpha;

        return "#" + red + green + blue + alpha;
    }
}
