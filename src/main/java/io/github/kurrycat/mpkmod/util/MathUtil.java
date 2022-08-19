package io.github.kurrycat.mpkmod.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MathUtil {
    public static String formatDecimals(double value, int decimals, boolean keepZeros) {
        if(keepZeros) {
            return String.format(Locale.US, "%." + decimals + "f", value);
        }
        String pattern;
        if (decimals == 0) pattern = "###";
        else pattern = "###." + new String(new char[decimals]).replace("\0", "#");
        return new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.US)).format(value);
    }

    public static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
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
}
