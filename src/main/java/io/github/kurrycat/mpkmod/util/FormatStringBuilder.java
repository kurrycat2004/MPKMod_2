package io.github.kurrycat.mpkmod.util;

public class FormatStringBuilder {
    private String formatString = "";

    public FormatStringBuilder addString(String s) {
        formatString += s;
        return this;
    }

    public FormatStringBuilder setColor(Colors c) {
        formatString += "{" + c.getName() + "}";
        return this;
    }

    public FormatStringBuilder addVar(String var) {
        formatString += "{" + var + "}";
        return this;
    }
    public FormatStringBuilder addVar(String var, int decimals) {
        formatString += "{" + var + "," + decimals + "}";
        return this;
    }

    public String toString() {
        return formatString;
    }
}
