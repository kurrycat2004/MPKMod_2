package io.github.kurrycat.mpkmod.util;

public final class StringUtil {
    private StringUtil() {
    }

    /**
     * Joins a list of strings into a single string, with the list rotated so that the specified start element is at the beginning.<br>
     * Also adds the start element at the end of the string. <br>
     * Example: <code>joinCycle(["a", "b", "c", "d"], "c", ", ") -> "c, d, a, b, c"</code>
     *
     * @param cycle        The list of strings to join
     * @param startElement The element to start the cycle from
     * @param separator    The separator to use between elements
     * @return The joined string
     */
    public static String joinCycle(Iterable<String> cycle, String startElement, String separator) {
        StringBuilder firstHalf = new StringBuilder();
        StringBuilder secondHalf = new StringBuilder();
        StringBuilder current = secondHalf;

        for (String id : cycle) {
            if (id.equals(startElement)) current = firstHalf;
            if (!current.isEmpty()) current.append(separator);
            current.append(id);
        }

        if (!secondHalf.isEmpty()) {
            firstHalf.append(separator).append(secondHalf);
        }

        firstHalf.append(separator).append(startElement);
        return firstHalf.toString();
    }
}
