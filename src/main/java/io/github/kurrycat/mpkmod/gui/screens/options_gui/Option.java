package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Option {
    private String name;
    private String value;
    private String defaultValue;
    private ValueType type;

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
        this.value = value;
        return this;
    }

    public Option setBoolean(Boolean value) {
        if (ValueType.BOOLEAN.equals(this.type))
            this.setValue(value.toString());
        return this;
    }

    public Boolean getBoolean() {
        if(this.type == ValueType.BOOLEAN)
            return this.value.equals("true");
        return null;
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

    public enum ValueType {
        STRING,
        DOUBLE,
        INTEGER,
        BOOLEAN,
        COLOR;
    }
}
