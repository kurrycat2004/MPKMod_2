package io.github.kurrycat.mpkmod.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MathUtil {
    public static String formatDecimals(double value, int decimals, boolean keepZeros) {
        if (keepZeros) {
            return String.format(Locale.US, "%." + decimals + "f", value);
        }
        String pattern;
        if (decimals == 0) pattern = "###";
        else pattern = "###." + new String(new char[decimals]).replace("\0", "#");
        return new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.US)).format(value);
    }

    public static Integer parseInt(String value, Integer defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    public static Double parseDouble(String value, Double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    public static int constrain(int value, int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return Math.max(min, Math.min(max, value));
    }

    public static int sqr(int v) {
        return v * v;
    }

    public static double strictMap(double v, double from, double to, double newFrom, double newTo) {
        return constrain(map(v, from, to, newFrom, newTo), newFrom, newTo);
    }

    /**
     * @param value value to constrain
     * @param min   min bounds
     * @param max   max bounds
     * @return min if value < min, max if value > max or else value
     */
    public static double constrain(double value, double min, double max) {
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * @param v       value to be mapped
     * @param from    min bound of the range of value
     * @param to      max bound of the range of value
     * @param newFrom new min bound for the mapped value
     * @param newTo   new max boung for the mapped value
     * @return v mapped between newFrom and newTo
     */
    public static double map(double v, double from, double to, double newFrom, double newTo) {
        return ((v - from) / (to - from)) * (newTo - newFrom) + newFrom;
    }

    public static int map(int v, int from, int to, int newFrom, int newTo) {
        return (int) map(v, from, to, newFrom, (double) newTo);
    }

    public static double roundToStep(double value, double step) {
        return Math.round(value / step) * step;
    }

    public static double distance(double v1, double v2) {
        return Math.abs(v1 - v2);
    }

    public static float wrapDegrees(float value) {
        value = value % 360.0F;

        if (value >= 180.0F) {
            value -= 360.0F;
        }

        if (value < -180.0F) {
            value += 360.0F;
        }

        return value;
    }
}
