package io.github.kurrycat.mpkmod.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ArrayListUtil {
    /**
     * Runs <code>elementSupplier</code> for every element of <code>list</code>, until it returns true.<br>
     * Info: Returns after the first true return of <code>elementSupplier</code>
     *
     * @param list            list of elements of type T
     * @param elementSupplier supplier that takes an element as argument and returns a boolean
     * @param <T>             any type
     * @return true if <code>elementSupplier</code> returned true for any element or else false
     */
    public static <T> boolean orMap(List<T> list, ListElementSupplier<T> elementSupplier) {
        for (T e : list)
            if (elementSupplier.apply(e)) return true;
        return false;
    }

    public static <T> boolean orMapAll(List<T> list, ListElementSupplier<T> elementSupplier) {
        boolean b = false;
        for (T e : list)
            if (elementSupplier.apply(e)) b = true;
        return b;
    }

    /**
     * @param tClass any class
     * @param list   any list
     * @param <T>    the type to filter for
     * @return a list containing any element of <code>list</code> for that <code>element instanceof tClass</code> is true
     */
    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> getAllOfType(Class<T> tClass, ArrayList<?> list) {
        return list.stream()
                .filter(tClass::isInstance)
                .map(c -> (T) c).collect(Collectors.toCollection(ArrayList<T>::new));
    }

    @SuppressWarnings("unchecked")
    public static <T> ArrayList<T> getAllOfType(Class<T> tClass, Object... list) {
        return (ArrayList<T>) Arrays.stream(list)
                .filter(Objects::nonNull)
                .filter(tClass::isInstance)
                .map(c -> (T) c).collect(Collectors.toCollection(ArrayList<T>::new));
    }

    /**
     * @param tClass any class
     * @param lists  any list of lists
     * @param <T>    any type
     * @return the result of {@link ArrayListUtil#getAllOfType getAllOfType(tClass, list)} for every list in lists combined
     */
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
