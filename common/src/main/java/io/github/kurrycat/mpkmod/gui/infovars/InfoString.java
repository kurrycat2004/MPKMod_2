package io.github.kurrycat.mpkmod.gui.infovars;

import io.github.kurrycat.mpkmod.Main;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.util.*;

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
    public static final String FORMATTING_REGEX = "\\{(?<varName>.*?)(,(?<decimals>\\d+)(?<keepZeros>!)?)?}";
    public static final Pattern FORMATTING_PATTERN = Pattern.compile(FORMATTING_REGEX);

    private final String[] textParts;
    private final Provider[] providers;

    public InfoString(String text) {
        String result = text;
        for (Colors c : Colors.values()) {
            result = result.replace("{" + c.getName() + "}", c.getCode());
        }

        Matcher matcher = FORMATTING_PATTERN.matcher(result);
        ArrayList<String> parts = new ArrayList<>();
        ArrayList<Provider> providerList = new ArrayList<>();

        int lastEnd = 0;
        while (matcher.find()) {
            parts.add(result.substring(lastEnd, matcher.start()));

            String fullMatch = matcher.group();
            String varName = matcher.group("varName");
            int decimals = MathUtil.parseInt(matcher.group("decimals"), 3);
            boolean keepZeros = matcher.group("keepZeros") != null;

            providerList.add(new Provider(fullMatch, varName, decimals, keepZeros));

            parts.add(null);
            lastEnd = matcher.end();
        }
        parts.add(result.substring(lastEnd));

        textParts = parts.toArray(new String[0]);
        providers = providerList.toArray(new Provider[0]);
    }

    public static Object getObj(List<Object> list) {
        if (list.size() == 1) return list.get(0);
        Object obj = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (obj == null) return null;

            Object o = list.get(i);
            if (o instanceof Method) {
                try {
                    obj = ((Method) o).invoke(obj);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    API.LOGGER.info(API.CONFIG_MARKER,
                            "InfoMap: IllegalAccessException while trying to access method {} from {}",
                            ((Method) o).getName(), obj.getClass().getName());
                    Debug.stacktrace();
                    return null;
                }
            } else if (o instanceof java.lang.reflect.Field) {
                try {
                    obj = ((java.lang.reflect.Field) o).get(obj);
                } catch (IllegalAccessException e) {
                    API.LOGGER.info(API.CONFIG_MARKER,
                            "InfoMap: IllegalAccessException while trying to access field {} from {}",
                            ((java.lang.reflect.Field) o).getName(), obj.getClass().getName());
                    Debug.stacktrace();
                    return null;
                }

            }
        }
        return obj;
    }

    public static InfoTree createInfoTree() {
        InfoTree infoMap = new InfoTree();

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

            InfoVar var = new InfoVar(name, objects);
            infoMap.addElement(name, var);

            if (dataClassList.contains(f.getType())) {
                recursiveSearch(var, objects, dataClassList);
            }
        }

        List<Tuple<Getter, Method>> methodAnnotations = ClassUtil.getMethodAnnotations(accessClassList, Getter.class);
        for (Tuple<Getter, Method> t : methodAnnotations) {
            Method m = t.getSecond();
            Class<?> clazz = m.getDeclaringClass();

            String name = StringUtil.getterName(m.getName());
            List<Object> objects = new ArrayList<>(accessInstances.get(clazz));
            objects.add(m);

            InfoVar var = new InfoVar(name, objects);
            infoMap.addElement(name, var);

            if (dataClassList.contains(m.getReturnType())) {
                recursiveSearch(var, objects, dataClassList);
            }
        }

        return infoMap;
    }

    private static void recursiveSearch(InfoVar parentVar, List<Object> parent, List<Class<?>> dataClassList) {
        if (parent.size() == 0) return;
        Object lastParent = parent.get(parent.size() - 1);
        Class<?> lastParentType = getParentType(lastParent);
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

            InfoVar var = parentVar.createChild(name, objects);

            if (dataClassList.contains(f.getType())) {
                //prevent infinite recursion
                if (!parent.contains(f))
                    recursiveSearch(var, objects, dataClassList);
            }
        }

        List<Tuple<Getter, Method>> methodAnnotations = ClassUtil.getMethodAnnotations(
                Collections.singletonList(lastParentType),
                Getter.class
        );
        for (Tuple<Getter, Method> t : methodAnnotations) {
            Method m = t.getSecond();

            String name = StringUtil.getterName(m.getName());
            List<Object> objects = new ArrayList<>(parent);
            objects.add(m);

            InfoVar var = parentVar.createChild(name, objects);

            if (dataClassList.contains(m.getReturnType())) {
                //prevent infinite recursion
                if (!parent.contains(m))
                    recursiveSearch(var, objects, dataClassList);
            }
        }
    }

    private static Class<?> getParentType(Object parent) {
        Class<?> parentType = null;
        if (parent instanceof Method)
            parentType = ((Method) parent).getReturnType();
        else if (parent instanceof java.lang.reflect.Field)
            parentType = ((java.lang.reflect.Field) parent).getType();
        return parentType;
    }

    public String get() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = 0; i < textParts.length; i++) {
            if (textParts[i] != null)
                sb.append(textParts[i]);
            else {
                sb.append(providers[j].get());
                j++;
            }
        }
        return sb.toString();
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
     * @see StringUtil#getterName(String)
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

    private static class Provider {
        private final String fullMatch;
        private final String varName;
        private final int decimals;
        private final boolean keepZeros;

        private final InfoVar infoVar;

        public Provider(String fullMatch, String varName, int decimals, boolean keepZeros) {
            this.fullMatch = fullMatch;
            this.varName = varName;
            this.decimals = decimals;
            this.keepZeros = keepZeros;

            this.infoVar = Main.infoTree.getElement(this.varName);
        }

        public String get() {
            if(this.infoVar == null) return fullMatch;
            Object o = this.infoVar.getObj();

            if (o == null) return fullMatch;

            if (o instanceof Double)
                return MathUtil.formatDecimals((Double) o, decimals, keepZeros);
            else if (o instanceof Float)
                return MathUtil.formatDecimals((Float) o, decimals, keepZeros);
            else if (o instanceof FormatDecimals)
                return ((FormatDecimals) o).formatDecimals(decimals, keepZeros);
            if(o.toString().equals(o.getClass().getName() + "@" + Integer.toHexString(o.hashCode())))
                return "[" + o.getClass().getSimpleName() + " object]";
            return o.toString();
        }
    }
}
