package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.gui.ComponentScreen;
import io.github.kurrycat.mpkmod.gui.components.Anchor;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.util.ArrayList;

public class MainGuiScreen extends ComponentScreen {
    public OptionsPane optionsPane = null;
    public LoadConfigPane loadConfigPane = null;
    public SaveConfigPane saveConfigPane = null;

    @Override
    public boolean shouldCreateKeyBind() {
        return true;
    }

    @Override
    public void onGuiInit() {
        super.onGuiInit();

        reloadConfig();

        addChild(new Button(
                "Save",
                new Vector2D(115, 5),
                new Vector2D(50, 20),
                mouseButton -> MainGuiScreen.this.openPane(saveConfigPane)
        ), PERCENT.NONE, Anchor.BOTTOM_RIGHT);

        addChild(
                new Button(
                        "Load File",
                        new Vector2D(60, 5),
                        new Vector2D(50, 20),
                        mouseButton -> MainGuiScreen.this.openPane(loadConfigPane)
                ),
                PERCENT.NONE, Anchor.BOTTOM_RIGHT
        );

        addChild(
                new Button(
                        "Options",
                        new Vector2D(5, 5),
                        new Vector2D(50, 20),
                        mouseButton -> MainGuiScreen.this.openPane(optionsPane)
                ),
                PERCENT.NONE, Anchor.BOTTOM_RIGHT
        );

        optionsPane = new OptionsPane(Vector2D.ZERO, new Vector2D(3 / 5D, 3 / 5D));
        passPositionTo(optionsPane, PERCENT.ALL, Anchor.CENTER);

        loadConfigPane = new LoadConfigPane(Vector2D.ZERO, new Vector2D(3 / 5D, 1));
        passPositionTo(loadConfigPane, PERCENT.ALL, Anchor.CENTER);

        saveConfigPane = new SaveConfigPane(Vector2D.ZERO, new Vector2D(3 / 5D, 1));
        passPositionTo(saveConfigPane, PERCENT.ALL, Anchor.CENTER);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        closeAllPanes();
        LabelConfiguration.currentConfig.saveInCustom();
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
    }

    @Override
    public void removeComponent(Component c) {
        LabelConfiguration.currentConfig.components.remove(c);
        reloadConfig();
    }

    @Override
    public void addComponent(Component c) {
        LabelConfiguration.currentConfig.components.add(c);
        reloadConfig();
    }

    public void reloadConfig() {
        movableComponents = new ArrayList<>(LabelConfiguration.currentConfig.components);
        movableComponents.forEach(this::passPositionTo);
    }
}
