package io.github.kurrycat.mpkmod.test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.gui.components.KeyBindingLabel;
import io.github.kurrycat.mpkmod.save.deserialize.ColorDeserializer;
import io.github.kurrycat.mpkmod.save.serialize.ColorSerializer;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.FormatStringBuilder;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class JSONTest {
    //@org.junit.jupiter.api.Test
    public void test() throws IOException {
        ArrayList<io.github.kurrycat.mpkmod.gui.components.Component> components = initComponents();
        System.out.println(components);
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        module.addSerializer(Color.class, new ColorSerializer());
        module.addDeserializer(Color.class, new ColorDeserializer());

        mapper.registerModule(module);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);

        String s = mapper.writeValueAsString(components);
        System.out.println(s);

        ArrayList<io.github.kurrycat.mpkmod.gui.components.Component> loaded = new ArrayList<>(Arrays.asList(mapper.readValue(s, io.github.kurrycat.mpkmod.gui.components.Component[].class)));

        System.out.println(loaded.get(0));
    }


    private ArrayList<io.github.kurrycat.mpkmod.gui.components.Component> initComponents() {
        ArrayList<Component> initComponents = new ArrayList<>();
        initComponents.add(
                new InfoLabel(
                        new FormatStringBuilder()
                                .addString("IP: ")
                                .setColor(Colors.WHITE)
                                .addVar("mc.IP")
                                .toString(),
                        new Vector2D(5, 5))
                        .setColor(Colors.GOLD.getColor()));

        initComponents.add(new InfoLabel("{gold}X: {white}{player.pos.x,5}", new Vector2D(5, 20)));
        initComponents.add(new InfoLabel("{gold}Y: {white}{player.pos.y,5}", new Vector2D(5, 30)));
        initComponents.add(new InfoLabel("{gold}Z: {white}{player.pos.z,5}", new Vector2D(5, 40)));
        initComponents.add(new InfoLabel("{gold}Yaw: {white}{player.yaw,5!} {gold}{player.facing}", new Vector2D(5, 50)));
        initComponents.add(new InfoLabel("{gold}Pitch: {white}{player.pitch,5}", new Vector2D(5, 60)));

        initComponents.add(new KeyBindingLabel(new Vector2D(-35, 70), "key.forward"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-50, 85), "key.left"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-35, 85), "key.back"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-20, 85), "key.right"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-50, 100), "key.sneak"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-28, 100), "key.sprint"));
        return initComponents;
    }
}
