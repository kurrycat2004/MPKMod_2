package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.gui.screens.MapOverviewGUI;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.FormatStringBuilder;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;

public class MainGuiScreen extends ComponentScreen {
    public MapOverviewGUI mapOverviewGUI = null;
    private ArrayList<Component> cachedElements;

    @Override
    public void onGuiInit() {
        super.onGuiInit();
        if (cachedElements == null) {
            ArrayList<Component> jsonElements = loadJSONComponents();
            cachedElements = jsonElements != null ? jsonElements : initComponents();
        }
        movableComponents = new ArrayList<>(cachedElements);

        components.add(new Button(
                "Reload From File",
                new Vector2D(
                        25,
                        Renderer2D.getScaledSize().getY() - 25
                ),
                new Vector2D(100, 20),
                mouseButton -> {
                    ArrayList<Component> jsonElements = loadJSONComponents();
                    cachedElements = jsonElements != null ? jsonElements : initComponents();
                    movableComponents = new ArrayList<>(cachedElements);
                }
        ));

        components.add(new Button(
                "Reset",
                new Vector2D(
                        Renderer2D.getScaledSize().getX() / 2D - 50,
                        Renderer2D.getScaledSize().getY() - 25
                ),
                new Vector2D(100, 20),
                mouseButton -> {
                    cachedElements = initComponents();
                    movableComponents = new ArrayList<>(cachedElements);
                }
        ));

        components.add(new Button(
                "MapOverview",
                new Vector2D(
                        Renderer2D.getScaledSize().getX() - 125,
                        Renderer2D.getScaledSize().getY() - 25
                ),
                new Vector2D(100, 20),
                mouseButton -> {
                    openPane(mapOverviewGUI);
                }
        ));

        mapOverviewGUI = new MapOverviewGUI(new Vector2D(20, 0), new Vector2D(Renderer2D.getScaledSize().getX() - 40, Renderer2D.getScaledSize().getY() + 5));

        //Runtime.getRuntime().addShutdownHook(new Thread(() -> Serializer.serialize(JSONConfig.configFile, components)));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        mapOverviewGUI.close();
        Serializer.serialize(JSONConfig.configFile, movableComponents);
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
    }

    private ArrayList<Component> loadJSONComponents() {
        Component[] deserializedInfo = Serializer.deserialize(JSONConfig.configFile, Component[].class);
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

        int i = 7;
        initComponents.add(new InfoLabel("{gold}Speed: {white}[{player.speed.x,5!}, {player.speed.y,3!}, {player.speed.z,5!}]", new Vector2D(5, i++ * 10)));
        initComponents.add(new InfoLabel("{gold}Time: {white}{mc.time}", new Vector2D(5, i++ * 10)));
        initComponents.add(new InfoLabel("{gold}Date: {white}{mc.date}", new Vector2D(5, i++ * 10)));
        initComponents.add(new InfoLabel("{gold}Last Turning: {white}{player.deltaYaw,5}", new Vector2D(5, i++ * 10)));
        initComponents.add(new InfoLabel("{gold}Airtime: {white}{player.airtime}", new Vector2D(5, i++ * 10)));
        initComponents.add(new InfoLabel("{gold}Tier: {white}{player.tier}", new Vector2D(5, i++ * 10)));
        initComponents.add(new InfoLabel("{gold}Last 45: {white}{player.last45,5}", new Vector2D(5, i++ * 10)));
        initComponents.add(new InfoLabel("{gold}Last Landing: {white}[{player.lastLanding.x,5}, {player.lastLanding.y,5}, {player.lastLanding.z,5}]", new Vector2D(5, i++ * 10)));
        initComponents.add(new InfoLabel("{gold}FPS: {white}{mc.FPS}", new Vector2D(5, i++ * 10 + 5)));

        initComponents.add(new KeyBindingLabel(new Vector2D(-48.0,  89.0), new Vector2D(20, 20), "key.forward", "W"));
        initComponents.add(new KeyBindingLabel(new Vector2D( -71.0, 112.0),  new Vector2D(20, 20), "key.left", "A"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-48.0, 112.0),  new Vector2D(20, 20), "key.back", "S"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-25.0, 112.0),  new Vector2D(20, 20), "key.right", "D"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-71.0, 135.0),  new Vector2D(32, 20), "key.sneak", "Shift"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-37.0, 135.0),  new Vector2D(32, 20), "key.sprint", "Ctrl"));
        initComponents.add(new KeyBindingLabel(new Vector2D(-71.0, 158.0),  new Vector2D(66, 20), "key.jump", "Space"));

        initComponents.add(new MessageQueue(new Vector2D(-35, 10), new Vector2D(30, 22), "Offsets"));
        return initComponents;
    }
}
