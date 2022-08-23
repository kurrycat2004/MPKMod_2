package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.components.InfoLabel;
import io.github.kurrycat.mpkmod.gui.components.KeyBindingLabel;
import io.github.kurrycat.mpkmod.save.Deserializer;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.FormatStringBuilder;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;

public class MainGuiScreen extends ComponentScreen {

    @Override
    public void onGuiInit() {
        super.onGuiInit();
        ArrayList<Component> jsonElements = loadJSONComponents();
        components = jsonElements != null ? jsonElements : initComponents();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Serializer.serialize(JSONConfig.configFile, components);
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
    }

    private ArrayList<Component> loadJSONComponents() {
        Component[] deserializedInfo = Deserializer.deserialize(JSONConfig.configFile, Component[].class);
        if (deserializedInfo == null) return null;
        return new ArrayList<>(Arrays.asList(deserializedInfo));
    }

    private ArrayList<Component> initComponents() {
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

        /*components.add(
                new InfoLabel(
                        new FormatStringBuilder()
                                .addString("Pos: [")
                                .setColor(Colors.WHITE)
                                .addVar("player.pos.x")
                                .setColor(Colors.GOLD)
                                .addString(", ")
                                .setColor(Colors.LIGHT_PURPLE)
                                .addVar("player.pos.y")
                                .setColor(Colors.GOLD)
                                .addString(", ")
                                .setColor(Colors.BLUE)
                                .addVar("player.pos.z")
                                .setColor(Colors.GOLD)
                                .addString("]")
                                .toString(),
                        new Vector2D(5, 30)
                )
                        .setColor(Colors.GOLD.getColor())
        );*/

        initComponents.add(new KeyBindingLabel(new Vector2D(-35, 70), "key.forward"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-50, 85), "key.left"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-35, 85), "key.back"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-20, 85), "key.right"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-50, 100), "key.sneak"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-28, 100), "key.sprint"));
        return initComponents;
    }
}
