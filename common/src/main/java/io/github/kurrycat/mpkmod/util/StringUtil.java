package io.github.kurrycat.mpkmod.util;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

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

    public static void copyToClipboard(String str) {
        StringSelection selection = new StringSelection(str);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
}
