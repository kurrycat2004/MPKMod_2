package io.github.kurrycat.mpkmod.save;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.gui.components.KeyBindingLabel;
import io.github.kurrycat.mpkmod.gui.components.Label;
import io.github.kurrycat.mpkmod.save.serialize.*;
import io.github.kurrycat.mpkmod.util.Vector2D;
import net.minecraft.client.settings.KeyBinding;

import java.awt.*;
import java.io.File;

public class SerializeManager {

    public static ObjectMapper mapper;
    public static SimpleModule module;

    public static void registerSerializer() {
        mapper = new ObjectMapper();
        module = new SimpleModule();

        module.addSerializer(Color.class, new ColorSerializer());
        module.addSerializer(InfoLabel.class, new InfoLabelSerializer());
        module.addSerializer(Label.class, new LabelSerializer());
        module.addSerializer(Vector2D.class, new Vector2DSerializer());
        module.addSerializer(KeyBinding.class, new KeyBindingSerializer());
        module.addSerializer(KeyBindingLabel.class, new KeyBindingLabelSerializer());

        mapper.registerModule(module);
    }

    public static <T> void serialize(File configFile, T infoLabel) {
        try {
            mapper.writeValue(configFile, infoLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> String serializeToString(T infoLabel) {
        try {
            return mapper.writeValueAsString(infoLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }

}
