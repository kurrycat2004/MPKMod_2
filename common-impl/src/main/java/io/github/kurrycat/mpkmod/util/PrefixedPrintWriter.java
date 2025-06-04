package io.github.kurrycat.mpkmod.util;

import java.io.PrintWriter;
import java.io.Writer;

public class PrefixedPrintWriter extends PrintWriter {
    private final String prefix;

    public PrefixedPrintWriter(String prefix, Writer out) {
        super(out);
        this.prefix = prefix;
    }

    @Override
    public void println(Object obj) {
        super.println(prefix + obj);
    }
}
