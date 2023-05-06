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

    public static String getterName(String str) {
        if (str == null || str.isEmpty()) return str;
        if(str.length() <= 3) return str;
        if(!str.startsWith("get")) return str;
        return str.substring(3, 4).toLowerCase() + str.substring(4);
    }
}
