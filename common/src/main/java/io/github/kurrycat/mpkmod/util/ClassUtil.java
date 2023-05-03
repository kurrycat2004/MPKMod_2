package io.github.kurrycat.mpkmod.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ClassUtil {
    public static Class<?> ModClass = null;

    public static <A extends Annotation> List<Tuple<A, Class<?>>> getClassAnnotations(List<Class<?>> classes, Class<A> annotationClass) {
        List<Tuple<A, Class<?>>> annotations = new ArrayList<>();
        for (Class<?> c : classes) {
            if (c.isAnnotationPresent(annotationClass)) {
                annotations.add(new Tuple<>(c.getAnnotation(annotationClass), c));
            }
        }
        return annotations;
    }

    public static <A extends Annotation> List<Tuple<A, Field>> getFieldAnnotations(List<Class<?>> classes, Class<A> annotationClass) {
        List<Tuple<A, Field>> annotations = new ArrayList<>();
        for (Class<?> c : classes) {
            for (Field f : c.getFields()) {
                if (f.isAnnotationPresent(annotationClass)) {
                    annotations.add(new Tuple<>(f.getAnnotation(annotationClass), f));
                }
            }
        }
        return annotations;
    }

    public static <A extends Annotation> List<Tuple<A, Method>> getMethodAnnotations(List<Class<?>> classes, Class<A> annotationClass) {
        List<Tuple<A, Method>> annotations = new ArrayList<>();
        for (Class<?> c : classes) {
            for (Method m : c.getMethods()) {
                if (m.isAnnotationPresent(annotationClass)) {
                    annotations.add(new Tuple<>(m.getAnnotation(annotationClass), m));
                }
            }
        }
        return annotations;
    }

    public static List<Class<?>> getClasses(final String pkgName) {
        final String pkgPath = pkgName.replace('.', '/');
        URI pkg;
        try {
            pkg = Objects.requireNonNull(ModClass.getClassLoader().getResource(pkgPath)).toURI();
        } catch (URISyntaxException | NullPointerException ignored) {
            return null;
        }

        final ArrayList<Class<?>> allClasses = new ArrayList<>();

        Path root;
        if (pkg.toString().startsWith("jar:")) {
            try {
                root = FileSystems.getFileSystem(pkg).getPath(pkgPath);
            } catch (final FileSystemNotFoundException e) {
                try {
                    root = FileSystems.newFileSystem(pkg, Collections.emptyMap()).getPath(pkgPath);
                } catch (IOException ignored) {
                    return null;
                }
            }
        } else {
            root = Paths.get(pkg);
        }

        final String extension = ".class";
        try {
            try (final Stream<Path> allPaths = Files.walk(root)) {
                allPaths.filter(Files::isRegularFile).forEach(file -> {
                    try {

                        final String path = file.toString().replace(file.getFileSystem().getSeparator(), ".");
                        System.out.println(path);
                        final String name = path.substring(path.indexOf(pkgName), path.length() - extension.length());
                        allClasses.add(Class.forName(name));
                    } catch (final ClassNotFoundException | StringIndexOutOfBoundsException ignored) {
                        ignored.printStackTrace();
                    }
                });
            }
        } catch (IOException ignored) {
            return null;
        }
        return allClasses;
    }

    public static void setModClass(Class<?> callerClass) {
        ModClass = callerClass;
    }
}
