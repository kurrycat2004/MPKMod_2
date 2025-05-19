package io.github.kurrycat.mpkmod.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public final class MultiParentClassLoader extends ClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final List<ClassLoader> parents;

    public MultiParentClassLoader(Collection<? extends ClassLoader> parents) {
        super(null);
        this.parents = List.copyOf(parents);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(name);
        if (clazz != null) {
            if (resolve) resolveClass(clazz);
            return clazz;
        }

        for (ClassLoader parent : parents) {
            try {
                clazz = parent.loadClass(name);
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (clazz == null) throw new ClassNotFoundException(name);

        if (resolve) resolveClass(clazz);
        return clazz;
    }


    @Override
    public URL getResource(String name) {
        for (ClassLoader parent : parents) {
            URL url = parent.getResource(name);
            if (url != null) return url;
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> list = new ArrayList<>();
        for (ClassLoader parent : parents) {
            list.addAll(Collections.list(parent.getResources(name)));
        }
        return Collections.enumeration(list);
    }
}

