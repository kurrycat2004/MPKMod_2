package io.github.kurrycat.mpkmod.save;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.save.deserialize.ColorDeserializer;
import io.github.kurrycat.mpkmod.save.deserialize.InfoLabelDeserializer;
import io.github.kurrycat.mpkmod.save.deserialize.KeyBindingDeserializer;
import io.github.kurrycat.mpkmod.save.deserialize.Vector2DDeserializer;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.settings.KeyBinding;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class DeserializeManager {

    public static ObjectMapper mapper;
    public static SimpleModule module;

    public static void registerDeserializer() {
        mapper = new ObjectMapper();
        module = new SimpleModule();

        module.addDeserializer(InfoLabel.class, new InfoLabelDeserializer());
        module.addDeserializer(Color.class, new ColorDeserializer());
        module.addDeserializer(Vector2D.class, new Vector2DDeserializer());
        module.addDeserializer(KeyBinding.class, new KeyBindingDeserializer());

        mapper.registerModule(module);
    }

    public static <T> T deserialize(File configFile, Class<T> c) {
        try {
            return mapper.readValue(configFile, c);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> T deserialize(String text, Class<T> c) {
        try {
            return mapper.readValue(text, c);
        } catch (IOException e) {
            return null;
        }
    }

}
