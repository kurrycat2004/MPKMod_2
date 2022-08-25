package io.github.kurrycat.mpkmod.gui.screens;

import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Pane;
import io.github.kurrycat.mpkmod.gui.components.TextLabel;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class MapOverviewGUI extends Pane {

    private boolean loaded;

    public MapOverviewGUI(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = Color.DARK_GRAY;
        this.loaded = false;
        initComponents();
        initButtons();
    }

    @Override
    public void render(Vector2D mouse) {
        if (loaded) {
            super.render(mouse);
        }
    }

    public void init() {
        this.loaded = true;
    }

    public void removeWindow() {
        this.loaded = false;
    }

    private void initComponents() {
        components.add(new TextLabel("Test Label", this.pos.add(50)));
    }

    private void initButtons() {
        buttons.add(
                new Button(
                    "X",
                    new Vector2D(this.getPos().getX() + this.getSize().getX() - 10, this.getPos().getY()),
                    new Vector2D(10, 10),
                    mouseButton -> {
                        removeWindow();
                    }
                )
        );
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

}
