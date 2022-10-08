package io.github.kurrycat.mpkmod.util;

public class StringUtil {
    /**
     * @param str string to be capitalized
     * @return first char of str in upper case + the rest of str
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
