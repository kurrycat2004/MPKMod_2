package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.ClassUtil;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Tuple;

import java.awt.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
    private String displayName = null;
    private String description = null;

    public Option(String name, String value, String defaultValue) {
        this(name, value, defaultValue, ValueType.STRING);
    }

    @JsonCreator
    public Option(@JsonProperty("name") String name, @JsonProperty("value") String value, @JsonProperty("defaultValue") String defaultValue, @JsonProperty("valueType") ValueType type) {
        this.name = name;
        this.value = value;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public Option(String name, Boolean value, Boolean defaultValue) {
        this(name, value.toString(), defaultValue.toString(), ValueType.BOOLEAN);
    }

    public static HashMap<String, Option> createOptionMap() {
        HashMap<String, Option> optionMap = new HashMap<>();

        //List<Class<?>> classes = ClassUtil.getClasses(API.packageName);

        /*if (classes.size() == 0) {
            API.LOGGER.warn(API.CONFIG_MARKER, "Error loading package while initializing option map");
            return optionMap;
        }*/

        List<Tuple<Field, java.lang.reflect.Field>> annotations = ClassUtil.getFieldAnnotations(Field.class);
        for (Tuple<Field, java.lang.reflect.Field> t : annotations) {
            Field a = t.getFirst();
            java.lang.reflect.Field f = t.getSecond();
            String id = f.getName();
            String value;
            try {
                value = f.get(f.getDeclaringClass()).toString();
            } catch (IllegalAccessException e) {
                API.LOGGER.debug(API.CONFIG_MARKER,
                        "OptionMap: IllegalAccessException while trying to access field {} from {}",
                        id, f.getDeclaringClass().getName());
                continue;
            }

            ValueType type = ValueType.STRING;
            if (f.getType() == Boolean.class || f.getType() == boolean.class) type = ValueType.BOOLEAN;
            if (f.getType() == Double.class || f.getType() == double.class) type = ValueType.DOUBLE;
            if (f.getType() == Integer.class || f.getType() == int.class) type = ValueType.INTEGER;
            if (f.getType() == Color.class) type = ValueType.COLOR;

            Option option = new Option(id, value, value, type)
                    .setCategory(a.category())
                    .setDisplayName(a.displayName())
                    .setDescription(a.description())
                    .link(f);
            optionMap.put(id, option);

            API.LOGGER.debug("Option of type {} added to map: {} with default value: {}", type, id, value);
        }

        List<Tuple<ChangeListener, Method>> listeners = ClassUtil.getMethodAnnotations(ChangeListener.class);
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

            API.LOGGER.debug("Option listener added for: {} ", fieldName);
        }

        API.LOGGER.info("{} Options registered", optionMap.size());

        return optionMap;
    }

    public Option link(java.lang.reflect.Field field) {
        this.linkedField = field;
        return this;
    }

    public static void updateOptionMapFromJSON(boolean suppressChangeListener) {
        HashMap<String, String> deserializedInfo = Serializer.deserializeAny(JSONConfig.optionsFile, new TypeReference<HashMap<String, String>>() {
        });
        if (deserializedInfo == null) return;

        for (String key : deserializedInfo.keySet()) {
            if (!API.optionsMap.containsKey(key)) continue;
            API.optionsMap.get(key).setValue(deserializedInfo.get(key), suppressChangeListener);
        }
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

    public Object getAsType() {
        switch (this.type) {
            case BOOLEAN:
                return getBoolean();
            case INTEGER:
                return getInteger();
            case DOUBLE:
                return getDouble();
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

    public Integer getInteger() {
        if (this.type == ValueType.INTEGER)
            return Integer.parseInt(this.value);
        return 0;
    }

    public Double getDouble() {
        if (this.type == ValueType.DOUBLE)
            return Double.parseDouble(this.value);
        return 0.0;
    }

    @JsonGetter("value")
    public String getValue() {
        return value;
    }

    public Option setValue(String value) {
        return setValue(value, false);
    }

    public String getDisplayName() {
        return displayName;
    }

    public Option setDisplayName(String s) {
        this.displayName = s;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Option setDescription(String s) {
        this.description = s;
        return this;
    }

    @JsonGetter("name")
    public String getName() {
        return name;
    }

    public Option setName(String name) {
        this.name = name;
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

    @SuppressWarnings("UnusedReturnValue")
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
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option option = (Option) o;
        return name.equals(option.name);
    }

    public enum ValueType {
        STRING,
        DOUBLE, //TODO: OptionListItemDouble
        INTEGER,
        BOOLEAN,
        COLOR, //TODO: OptionListItemColor
    }

    /**
     * Marks a field as a MPK-Option. The field has to be public.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Field {
        String category() default "";

        String displayName() default "";

        String description() default "";
    }

    /**
     * Marks a method as a MPK-Option change listener. The method has to be public.
     */
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
