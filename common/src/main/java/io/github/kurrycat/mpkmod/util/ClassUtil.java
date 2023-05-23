package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.compatibility.API;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClassUtil {
    public static Class<?> ModClass = null;

    private static Set<Class<?>> classes = null;

    public static <A extends Annotation> List<Tuple<A, Class<?>>> getClassAnnotations(Class<A> annotationClass) {
        return getClassAnnotations(classes(), annotationClass);
    }

    public static <A extends Annotation> List<Tuple<A, Class<?>>> getClassAnnotations(Collection<Class<?>> classes, Class<A> annotationClass) {
        List<Tuple<A, Class<?>>> annotations = new ArrayList<>();
        for (Class<?> c : classes) {
            if (c.isAnnotationPresent(annotationClass)) {
                annotations.add(new Tuple<>(c.getAnnotation(annotationClass), c));
            }
        }
        return annotations;
    }

    private static Set<Class<?>> classes() {
        if (classes == null) {
            InputStream in = FileUtil.getResource("classes.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            classes = new HashSet<>();
            try {
                String[] classList = reader.readLine().split(";");
                for (String s : classList) {
                    classes.add(Class.forName(s));
                }
            } catch (IOException | ClassNotFoundException ignored) {
            }
        }
        return classes;
    }

    public static <A extends Annotation> List<Tuple<A, Field>> getFieldAnnotations(Class<A> annotationClass) {
        return getFieldAnnotations(classes(), annotationClass);
    }

    public static <A extends Annotation> List<Tuple<A, Field>> getFieldAnnotations(Collection<Class<?>> classes, Class<A> annotationClass) {
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

    public static <A extends Annotation> List<Tuple<A, Method>> getMethodAnnotations(Class<A> annotationClass) {
        return getMethodAnnotations(classes(), annotationClass);
    }

    public static <A extends Annotation> List<Tuple<A, Method>> getMethodAnnotations(Collection<Class<?>> classes, Class<A> annotationClass) {
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

    public static List<Class<?>> classes(String packageName) {
        String path = packageName.replace(".", File.separator);
        String path2 = packageName.replace(".", "/");
        List<Class<?>> classes = new ArrayList<>();

        String classPaths = System.getProperty("java.class.path");
        if (classPaths == null || !classPaths.contains(path) && !classPaths.contains(path2)) {
            classPaths = System.getProperty("legacyClassPath");
        }
        if (classPaths == null) {
            return classes;
        }

        String[] classPathEntries = classPaths.split(System.getProperty("path.separator"));

        API.LOGGER.info(classPaths);

        String name;
        for (String classpathEntry : classPathEntries) {
            if (classpathEntry.endsWith(".jar")) {
                File jar = new File(classpathEntry);
                try {
                    JarInputStream is = new JarInputStream(Files.newInputStream(jar.toPath()));
                    JarEntry entry;
                    while ((entry = is.getNextJarEntry()) != null) {
                        name = entry.getName();
                        if (name.endsWith(".class")) {
                            if (name.contains(path) || name.contains(path2)) {
                                String classPath = name.substring(0, entry.getName().length() - 6);
                                classPath = classPath.replaceAll("[|/]", ".");
                                classes.add(Class.forName(classPath));
                            }
                        }
                    }
                } catch (Exception ex) {
                    // Silence is gold
                    API.LOGGER.debug("Exception during class loading: ", ex);
                }
            } else {
                try {
                    File base = new File(classpathEntry + File.separatorChar + path);
                    if (!base.isDirectory()) continue;
                    for (File file : Objects.requireNonNull(base.listFiles())) {
                        name = file.getName();
                        if (name.endsWith(".class")) {
                            name = name.substring(0, name.length() - 6);
                            classes.add(Class.forName(packageName + "." + name));
                        }
                    }
                } catch (Exception ex) {
                    // Silence is gold
                    API.LOGGER.debug("Exception during class loading: ", ex);
                }
            }
        }

        return classes;
    }

    /*public static List<Class<?>> getClasses(final String pkgName) {
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
    }*/

    public static void setModClass(Class<?> callerClass) {
        ModClass = callerClass;
    }
}
