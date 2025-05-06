package io.github.kurrycat.mpkmod.util;

import java.util.List;

public final class MultiParentClassLoader extends ClassLoader {
    private final List<ClassLoader> parents;

    public MultiParentClassLoader(List<ClassLoader> parents) {
        super(null);
        this.parents = parents;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader parent : parents) {
            try {
                return parent.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(name);
    }
}

