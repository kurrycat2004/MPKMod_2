package io.github.kurrycat.mpkmod.save;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kurrycat.mpkmod.save.deserialize.ColorDeserializer;
import io.github.kurrycat.mpkmod.save.serialize.ColorSerializer;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Serializer {

    public static ObjectMapper mapper;
    public static SimpleModule module;

    public static void registerSerializer() {
        mapper = new ObjectMapper();
        module = new SimpleModule();

        module.addSerializer(Color.class, new ColorSerializer());
        module.addDeserializer(Color.class, new ColorDeserializer());

        mapper.registerModule(module);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public static <T> void serialize(File configFile, T value) {
        try {
            mapper.writeValue(configFile, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void serializeWithoutTyping(File configFile, T value) {
        try {
            mapper.disableDefaultTyping();
            mapper.writeValue(configFile, value);
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        } catch (Exception e) {
            e.printStackTrace();
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
}
