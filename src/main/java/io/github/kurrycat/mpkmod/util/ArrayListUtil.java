package io.github.kurrycat.mpkmod.util;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ArrayListUtil {
    public static <T> boolean orMap(ArrayList<T> list, ListElementSupplier<T> elementSupplier) {
        for (T e : list)
            if (elementSupplier.apply(e)) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> getAllOfType(ArrayList<?> list, Class<T> tClass) {
        return list.stream()
                .filter(tClass::isInstance)
                .map(c -> (T) c).collect(Collectors.toCollection(ArrayList<T>::new));
    }

    @FunctionalInterface
    public interface ListElementSupplier<T> {
        boolean apply(T ele);
    }
}
