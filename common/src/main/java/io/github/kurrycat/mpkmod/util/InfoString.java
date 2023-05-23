package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Profiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class InfoString {
    @SuppressWarnings("all")
    public static final String FORMATTING_REGEX = "\\{(?<varName>.*?)(,(?<decimals>\\d+)(?<keepZeros>!)?)?}";
    public static final Pattern FORMATTING_PATTERN = Pattern.compile(FORMATTING_REGEX);

    public static String getFormattedText(String input) {
        Matcher matcher = FORMATTING_PATTERN.matcher(input);

        Profiler.startSection("colors");
        String result = input;
        for (Colors c : Colors.values()) {
            result = result.replace("{" + c.getName() + "}", c.getCode());
        }

        Profiler.endStartSection("vars");

        while (matcher.find()) {
            String fullMatch = matcher.group();
            String varName = matcher.group("varName");
            int decimals = MathUtil.parseInt(matcher.group("decimals"), 3);
            boolean keepZeros = matcher.group("keepZeros") != null;

            ObjectProvider provider = API.infoMap.get(varName);
            if (provider == null) continue;
            Object o = provider.getObj();

            if (o == null) continue;

            if (o instanceof Double)
                result = result.replace(fullMatch, MathUtil.formatDecimals((Double) o, decimals, keepZeros));
            else if (o instanceof Float)
                result = result.replace(fullMatch, MathUtil.formatDecimals((Float) o, decimals, keepZeros));
            else if (o instanceof FormatDecimals)
                result = result.replace(fullMatch, ((FormatDecimals) o).formatDecimals(decimals, keepZeros));
            result = result.replace(fullMatch, o.toString());
        }
        Profiler.endSection();
        return result;
    }

    private static Object getObj(List<Object> list) {
        if (list.size() == 1) return list.get(0);
        Object obj = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if(obj == null) return null;

            Object o = list.get(i);
            if (o instanceof Method) {
                try {
                    obj = ((Method) o).invoke(obj);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    API.LOGGER.info(API.CONFIG_MARKER,
                            "InfoMap: IllegalAccessException while trying to access method {} from {}",
                            ((Method) o).getName(), obj.getClass().getName());
                    return null;
                }
            } else if (o instanceof java.lang.reflect.Field) {
                try {
                    obj = ((java.lang.reflect.Field) o).get(obj);
                } catch (IllegalAccessException e) {
                    API.LOGGER.info(API.CONFIG_MARKER,
                            "InfoMap: IllegalAccessException while trying to access field {} from {}",
                            ((java.lang.reflect.Field) o).getName(), obj.getClass().getName());
                    return null;
                }

            }
        }
        return obj;
    }

    public static HashMap<String, ObjectProvider> createInfoMap() {
        HashMap<String, ObjectProvider> infoMap = new HashMap<>();

        //List<Class<?>> classes = ClassUtil.getClasses(API.packageName);

       /* if (classes == null || classes.size() == 0) {
            API.LOGGER.warn(API.CONFIG_MARKER, "Error loading package while initializing option map");
            return infoMap;
        }*/

        HashMap<Class<?>, List<Object>> accessInstances = new HashMap<>();
        List<Tuple<AccessInstance, java.lang.reflect.Field>> accessInstanceAnnotations = ClassUtil.getFieldAnnotations(AccessInstance.class);
        for (Tuple<AccessInstance, java.lang.reflect.Field> a : accessInstanceAnnotations) {
            java.lang.reflect.Field field = a.getSecond();
            Class<?> accessClass = field.getDeclaringClass();
            accessInstances.put(accessClass, Arrays.asList(accessClass, field));
        }
        List<Tuple<AccessInstance, Class<?>>> accessClassAnnotations = ClassUtil.getClassAnnotations(AccessInstance.class);
        for (Tuple<AccessInstance, Class<?>> a : accessClassAnnotations) {
            Class<?> clazz = a.getSecond();
            accessInstances.put(clazz, Collections.singletonList(clazz));
        }

        List<Tuple<DataClass, Class<?>>> dataClassAnnotations = ClassUtil.getClassAnnotations(DataClass.class);
        List<Class<?>> dataClassList = dataClassAnnotations.stream().map(Tuple::getSecond).collect(Collectors.toList());

        List<Class<?>> accessClassList = new ArrayList<>(accessInstances.keySet());

        List<Tuple<Field, java.lang.reflect.Field>> fieldAnnotations = ClassUtil.getFieldAnnotations(accessClassList, Field.class);

        for (Tuple<Field, java.lang.reflect.Field> t : fieldAnnotations) {
            java.lang.reflect.Field f = t.getSecond();
            Class<?> clazz = f.getDeclaringClass();

            String name = f.getName();
            List<Object> objects = new ArrayList<>(accessInstances.get(clazz));
            objects.add(f);

            infoMap.put(name, () -> {
                Object o = getObj(objects);
                return o == null ? "undefined" : o;
            });
            if (dataClassList.contains(f.getType())) {
                recursiveSearch(infoMap, name, new ArrayList<>(objects), dataClassList);
            }
        }

        List<Tuple<Getter, Method>> methodAnnotations = ClassUtil.getMethodAnnotations(accessClassList, Getter.class);
        for (Tuple<Getter, Method> t : methodAnnotations) {
            Method m = t.getSecond();
            Class<?> clazz = m.getDeclaringClass();

            String methodName = m.getName();
            String name = StringUtil.getterName(methodName);
            List<Object> objects = new ArrayList<>(accessInstances.get(clazz));
            objects.add(m);

            infoMap.put(name, () -> {
                Object o = getObj(objects);
                return o == null ? "undefined" : o;
            });
            if (dataClassList.contains(m.getReturnType())) {
                recursiveSearch(infoMap, name, objects, dataClassList);
            }
        }

        return infoMap;
    }

    private static void recursiveSearch(Map<String, ObjectProvider> infoMap, String parentName, List<Object> parent, List<Class<?>> dataClassList) {
        if (parent.size() == 0) return;
        Object lastParent = parent.get(parent.size() - 1);
        Class<?> lastParentType = null;
        if (lastParent instanceof Method)
            lastParentType = ((Method) lastParent).getReturnType();
        else if (lastParent instanceof java.lang.reflect.Field)
            lastParentType = ((java.lang.reflect.Field) lastParent).getType();

        if (lastParentType == null) return;

        List<Tuple<Field, java.lang.reflect.Field>> fieldAnnotations = ClassUtil.getFieldAnnotations(
                Collections.singletonList(lastParentType),
                Field.class
        );
        for (Tuple<Field, java.lang.reflect.Field> t : fieldAnnotations) {
            java.lang.reflect.Field f = t.getSecond();

            String name = f.getName();
            List<Object> objects = new ArrayList<>(parent);
            objects.add(f);

            infoMap.put(parentName + "." + name, () -> {
                Object o = getObj(objects);
                return o == null ? "undefined" : o;
            });
            if (dataClassList.contains(f.getType())) {
                //prevent infinite recursion
                if (!objects.subList(0, objects.size() - 1).contains(f))
                    recursiveSearch(infoMap, parentName + "." + name, objects, dataClassList);
            }
        }

        List<Tuple<Getter, Method>> methodAnnotations = ClassUtil.getMethodAnnotations(
                Collections.singletonList(lastParentType),
                Getter.class
        );
        for (Tuple<Getter, Method> t : methodAnnotations) {
            Method m = t.getSecond();

            String methodName = m.getName();
            String name = StringUtil.getterName(methodName);
            List<Object> objects = new ArrayList<>(parent);
            objects.add(m);

            infoMap.put(parentName + "." + name, () -> {
                Object o = getObj(objects);
                return o == null ? "undefined" : o;
            });
            if (dataClassList.contains(m.getReturnType())) {
                //prevent infinite recursion
                if (!objects.contains(m))
                    recursiveSearch(infoMap, parentName + "." + name, objects, dataClassList);
            }
        }
    }

    public static List<String> getInfoVarsList() {
        return API.infoMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    /**
     * Variable name is field name
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Field {
    }

    /**
     * Method that takes no arguments.
     * Variable name is method name without get
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Getter {
    }

    /**
     * public static instance field {@link Field} and {@link Getter} will search for their current value, or class if all {@link Field} and {@link Getter} are static
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    public @interface AccessInstance {
    }

    /**
     * Class that is a return type of some {@link AccessInstance} and should have its own {@link Getter}s and {@link Field}s
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface DataClass {
    }

    @FunctionalInterface
    public interface ObjectProvider {
        Object getObj();
    }
}
