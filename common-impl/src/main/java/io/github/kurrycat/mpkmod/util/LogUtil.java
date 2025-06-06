package io.github.kurrycat.mpkmod.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class LogUtil {
    public static StringWriter appendPrefixedException(StringWriter writer, String prefix, Exception e) {
        try (PrintWriter w = new PrefixedPrintWriter(prefix, writer)) {
            e.printStackTrace(w);
        } catch (Exception printException) {
            writer.append(prefix);
            writer.append("failed to print stack trace: ");
            writer.append(printException.toString());
        }
        return writer;
    }
}
