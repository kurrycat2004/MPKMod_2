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
    public static <T> ArrayList<T> getAllOfType(Class<T> tClass, ArrayList<?> list) {
        return list.stream()
                .filter(tClass::isInstance)
                .map(c -> (T) c).collect(Collectors.toCollection(ArrayList<T>::new));
    }

    public static <T> ArrayList<T> getAllOfType(Class<T> tClass, ArrayList<?>... lists) {
        ArrayList<T> returnList = new ArrayList<>();
        for (ArrayList<?> list : lists) {
            returnList.addAll(getAllOfType(tClass, list));
        }
        return returnList;
    }


    @SafeVarargs
    public static <T> ArrayList<T> joinLists(ArrayList<T>... lists) {
        if (lists.length == 0) return null;
        ArrayList<T> returnList = new ArrayList<>(lists[0]);
        for (int i = 1; i < lists.length; i++)
            if (lists[i] != null)
                returnList.addAll(lists[i]);
        return returnList;
    }

    @FunctionalInterface
    public interface ListElementSupplier<T> {
        boolean apply(T ele);
    }
}
