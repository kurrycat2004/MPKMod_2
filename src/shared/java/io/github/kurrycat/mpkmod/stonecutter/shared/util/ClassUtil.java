package io.github.kurrycat.mpkmod.stonecutter.shared.util;

public final class ClassUtil {
    private ClassUtil() {}

    public static boolean isClassLoaded(String className) {
        try {
            Class.forName(className, false, ClassUtil.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
