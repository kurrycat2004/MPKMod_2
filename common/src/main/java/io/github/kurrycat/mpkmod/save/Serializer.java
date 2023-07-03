package io.github.kurrycat.mpkmod.save;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.save.deserialize.ColorDeserializer;
import io.github.kurrycat.mpkmod.save.serialize.ColorSerializer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Serializer {
    public static ObjectMapper mapper;
    public static SimpleModule module;
    public static TypeFactory typeFactory;

    public static void registerSerializer() {
        mapper = new ObjectMapper();
        module = new SimpleModule();
        typeFactory = mapper.getTypeFactory();

        module.addSerializer(Color.class, new ColorSerializer());
        module.addDeserializer(Color.class, new ColorDeserializer());

        mapper.registerModule(module);
        PolymorphicTypeValidator polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Component.class).build();
        mapper.activateDefaultTyping(polymorphicTypeValidator, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        //for backwards compatibility, make sure to set default fields in classes getting deserialized
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
    }

    public static <T> void serialize(File configFile, T value) {
        try {
            mapper.writeValue(configFile, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String serializeAsString(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T deserialize(File configFile, Class<T> c) {
        if (!configFile.exists()) return null;
        try {
            return mapper.readValue(configFile, c);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T deserializeString(String json, Class<T> c) {
        try {
            return mapper.readValue(json, c);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T deserialize(InputStream configFile, Class<T> c) {
        try {
            return mapper.readValue(configFile, c);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T deserializeAny(File configFile, TypeReference<T> typeReference) {
        if (!configFile.exists()) return null;
        try {
            return mapper.readValue(configFile, typeReference);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T deserializeAny(InputStream configFile, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(configFile, typeReference);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
