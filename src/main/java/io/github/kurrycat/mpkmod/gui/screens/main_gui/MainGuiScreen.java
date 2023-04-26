package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.gui.ComponentScreen;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;

public class MainGuiScreen extends ComponentScreen {
    public OptionsPane optionsPane = null;
    public ArrayList<Component> cachedElements;
    private boolean isCached = false;

    @Override
    public boolean shouldCreateKeyBind() {
        return true;
    }

    @Override
    public void onGuiInit() {
        super.onGuiInit();
        if (cachedElements == null) {
            cachedElements = loadJSONComponents();
        } else {
            isCached = true;
        }
        movableComponents = new ArrayList<>(cachedElements);

        Vector2D windowSize = getScreenSize();

        addChild(
                new Button(
                        "Options",
                        new Vector2D(5, 5),
                        new Vector2D(50, 20),
                        mouseButton -> openPane(optionsPane)
                ),
                false, false, Component.Anchor.BOTTOM_RIGHT
        );

        optionsPane = new OptionsPane(
                new Vector2D(windowSize.getX() / 5, windowSize.getY() / 5),
                new Vector2D(windowSize.getX() / 5 * 3, windowSize.getY() / 5 * 3)
        );
    }

    @Override
    public void removeComponent(Component c) {
        cachedElements.remove(c);
        movableComponents = new ArrayList<>(cachedElements);
    }

    @Override
    public void addComponent(Component c) {
        cachedElements.add(c);
        movableComponents = new ArrayList<>(cachedElements);
    }

    @Override
    public void onGuiClosed() {
        isCached = false;
        super.onGuiClosed();
        optionsPane.close();
        Serializer.serialize(JSONConfig.configFile, movableComponents);
    }

    public boolean isCached() {
        return isCached;
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
    }

    public ArrayList<Component> loadJSONComponents() {
        Component[] deserializedInfo = Serializer.deserialize(JSONConfig.configFile, Component[].class);

        if (deserializedInfo == null)
            deserializedInfo = Serializer.deserialize(JSONConfig.defaultConfigURL, Component[].class);

        if (deserializedInfo == null) return new ArrayList<>();

        return new ArrayList<>(Arrays.asList(deserializedInfo));
    }
}
