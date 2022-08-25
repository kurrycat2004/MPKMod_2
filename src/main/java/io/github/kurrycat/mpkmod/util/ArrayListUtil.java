package io.github.kurrycat.mpkmod.util;

import java.util.ArrayList;

public class ArrayListUtil {
    public static <T> boolean orMap(ArrayList<T> list, ListElementSupplier<T> elementSupplier) {
        for (T e : list)
            if (elementSupplier.apply(e)) return true;
        return false;
    }

    @FunctionalInterface
    public interface ListElementSupplier<T> {
        boolean apply(T ele);
    }
}
