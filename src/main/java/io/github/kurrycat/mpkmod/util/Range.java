package io.github.kurrycat.mpkmod.util;

public class Range {
    private final Integer lower;
    private final Integer upper;

    public Range(Integer lower, Integer upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public boolean includes(int v) {
        if (upper == null && lower == null) return true;
        else if (upper == null) return v >= lower;
        else if (lower == null) return v <= upper;
        else return v >= lower && v <= upper;
    }

    public int constrain(int v) {
        if (lower != null && v < lower) return lower;
        if (upper != null && v > upper) return upper;
        return v;
    }

    public boolean isAbove(int v) {
        if (upper == null) return false;
        return v > upper;
    }
}
