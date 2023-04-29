package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.gui.ComponentScreen;
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

        addChild(
                new Button(
                        "Save",
                        new Vector2D(115, 5),
                        new Vector2D(50, 20),
                        mouseButton -> MainGuiScreen.this.openPane(saveConfigPane)
                ),
                false, false, Component.Anchor.BOTTOM_RIGHT
        );

        addChild(
                new Button(
                        "Load File",
                        new Vector2D(60, 5),
                        new Vector2D(50, 20),
                        mouseButton -> MainGuiScreen.this.openPane(loadConfigPane)
                ),
                false, false, Component.Anchor.BOTTOM_RIGHT
        );

        addChild(
                new Button(
                        "Options",
                        new Vector2D(5, 5),
                        new Vector2D(50, 20),
                        mouseButton -> MainGuiScreen.this.openPane(optionsPane)
                ),
                false, false, Component.Anchor.BOTTOM_RIGHT
        );

        optionsPane = new OptionsPane(new Vector2D(0.5D, 0.5D), new Vector2D(3 / 5D, 3 / 5D));
        optionsPane.setParent(this, true, true, true, true);

        loadConfigPane = new LoadConfigPane(new Vector2D(0.5D, 0.5D), new Vector2D(3 / 5D, 1));
        loadConfigPane.setParent(this, true, true, true, true);

        saveConfigPane = new SaveConfigPane(new Vector2D(0.5D, 0.5D), new Vector2D(3 / 5D, 1));
        saveConfigPane.setParent(this, true, true, true, true);
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

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        optionsPane.close();
        loadConfigPane.close();
        saveConfigPane.close();
        LabelConfiguration.currentConfig.saveInCustom();
    }

    public void reloadConfig() {
        movableComponents = new ArrayList<>(LabelConfiguration.currentConfig.components);
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
    }
}
