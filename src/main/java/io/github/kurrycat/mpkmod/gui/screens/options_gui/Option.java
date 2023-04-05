package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.ClassUtil;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Tuple;

import java.awt.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Option {
    private String name;
    private String value;
    private String defaultValue;
    private ValueType type;
    private String category = "";
    private OptionChangeListener changeListener;
    private java.lang.reflect.Field linkedField = null;

    @JsonCreator
    public Option(@JsonProperty("name") String name, @JsonProperty("value") String value, @JsonProperty("defaultValue") String defaultValue, @JsonProperty("valueType") ValueType type) {
        this.name = name;
        this.value = value;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public Option(String name, String value, String defaultValue) {
        this(name, value, defaultValue, ValueType.STRING);
    }

    public Option(String name, Boolean value, Boolean defaultValue) {
        this(name, value.toString(), defaultValue.toString(), ValueType.BOOLEAN);
    }

    public static HashMap<String, Option> createOptionMap() {
        HashMap<String, Option> optionMap = new HashMap<>();

        List<Class<?>> classes = ClassUtil.getClasses(API.packageName);

        if (classes == null) {
            API.LOGGER.warn(API.CONFIG_MARKER, "Error loading package while initializing option map");
            return optionMap;
        }

        List<Tuple<Field, java.lang.reflect.Field>> annotations = ClassUtil.getFieldAnnotations(classes, Field.class);
        for (Tuple<Field, java.lang.reflect.Field> t : annotations) {
            Field a = t.getFirst();
            java.lang.reflect.Field f = t.getSecond();
            String name = f.getName();
            String value;
            try {
                value = f.get(f.getDeclaringClass()).toString();
            } catch (IllegalAccessException e) {
                API.LOGGER.info(API.CONFIG_MARKER,
                        "OptionMap: IllegalAccessException while trying to access field {} from {}",
                        name, f.getDeclaringClass().getName());
                continue;
            }
            String category = a.category();
            ValueType type = ValueType.STRING;
            if (f.getType() == Boolean.class || f.getType() == boolean.class) type = ValueType.BOOLEAN;
            if (f.getType() == Double.class || f.getType() == double.class) type = ValueType.DOUBLE;
            if (f.getType() == Integer.class || f.getType() == int.class) type = ValueType.INTEGER;
            if (f.getType() == Color.class) type = ValueType.COLOR;

            Option option = new Option(name, value, value, type).setCategory(category).link(f);
            optionMap.put(name, option);

            API.LOGGER.info("Option added to map: {} with default value: {}", name, value);
        }

        List<Tuple<ChangeListener, Method>> listeners = ClassUtil.getMethodAnnotations(classes, ChangeListener.class);
        for (Tuple<ChangeListener, Method> l : listeners) {
            ChangeListener c = l.getFirst();
            Method m = l.getSecond();

            String fieldName = c.field();

            if (!optionMap.containsKey(fieldName))
                continue;

            optionMap.get(fieldName).setChangeListener(() -> {
                try {
                    m.invoke(m.getDeclaringClass());
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    System.out.println("failed to invoke");
                }
            });

            API.LOGGER.info("Option listener added for: {} ", fieldName);
        }

        return optionMap;
    }

    public static void updateOptionMapFromJSON(boolean suppressChangeListener) {
        HashMap<String, String> deserializedInfo = Serializer.deserializeRaw(JSONConfig.optionsFile);
        if (deserializedInfo == null) return;

        for (String key : deserializedInfo.keySet()) {
            if (!API.optionsMap.containsKey(key)) continue;
            API.optionsMap.get(key).setValue(deserializedInfo.get(key), suppressChangeListener);
        }
    }


    @JsonGetter("name")
    public String getName() {
        return name;
    }

    public Option setName(String name) {
        this.name = name;
        return this;
    }

    @JsonGetter("value")
    public String getValue() {
        return value;
    }

    public Option setValue(String value) {
        return setValue(value, false);
    }

    public Option setValue(String value, boolean suppressChangeListener) {
        if (this.value.equals(value))
            return this;

        this.value = value;

        if (this.linkedField != null) {
            try {
                this.linkedField.set(this.linkedField.getDeclaringClass(), this.getAsType());
            } catch (IllegalAccessException ignored) {
            }
        }

        if (!suppressChangeListener && changeListener != null)
            changeListener.onOptionChange();
        return this;
    }

    public Option link(java.lang.reflect.Field field) {
        this.linkedField = field;
        return this;
    }

    public Object getAsType() {
        switch (this.type) {
            case BOOLEAN:
                return getBoolean();
            default:
                return getValue();
        }
    }

    public Boolean getBoolean() {
        if (this.type == ValueType.BOOLEAN)
            return this.value.equals("true");
        return null;
    }

    public Option setBoolean(Boolean value) {
        if (ValueType.BOOLEAN.equals(this.type))
            this.setValue(value.toString());
        return this;
    }

    public String getCategory() {
        return category;
    }

    public Option setCategory(String category) {
        this.category = category;
        return this;
    }

    public OptionChangeListener getChangeListener() {
        return changeListener;
    }

    public Option setChangeListener(OptionChangeListener changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    @JsonGetter("defaultValue")
    public String getDefaultValue() {
        return defaultValue;
    }

    public Option setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @JsonGetter("valueType")
    public ValueType getType() {
        return type;
    }

    public Option setType(ValueType type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option option = (Option) o;
        return name.equals(option.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public enum ValueType {
        STRING,
        DOUBLE,
        INTEGER,
        BOOLEAN,
        COLOR;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Field {
        String category() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ChangeListener {
        String field();
    }

    @FunctionalInterface
    public interface OptionChangeListener {
        void onOptionChange();
    }
}
